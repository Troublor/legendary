package dos_connector;

import com.google.gson.Gson;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;
/*

  DosConnector:
  连接 dos, 连接过程会引起阻塞
  dc = new DosConnector(12345)

  汇编指源代码，暂时只支持单文件，后面再支持多文件
  dc.masm(String asm_file)

  连接指定 obj文件
  dc.link(String obj_file)

  指定可执行文件进行 debug, 可执行文件需要在 DosOnAir/dosfiles文件夹下
  dc.startDebug('sample.exe')

  运行指定步数
  dc.step(n)

  检查当前寄存器状态
  dc.getRegister()

  检查指定内存处的内容
  dc.displayData(from, to)

  以上方法都没有返回值

  要获取返回数据
  1.需要先注册回调函数
  void do_something(DosInfo info);
  info 中包含了 DOS 的运行信息。每次 DosConnector 收到 DOS 的返回数据
  （包括程序的输出，dc.getRegister()等方法导致的寄存器数据返回），
  这个方法都会被调用。可以在这个方法中更新寄存器窗口的UI等等

  2.并需要启动后台服务
  dc.startListen()
  这个方法会启动一个线程对与 DOS 连接的 soket 进行轮询，当读取到数据时，会调用回调函数

  # todo 实际需要两个回调函数，一个处理 dc.step() 这种debug程序返回的寄存器数据
  # todo 另一个处理用户程序本身在标准输出进行的输出

  一些说明：
  为什么不使用返回值直接返回 DOS 接下来返回的数据，而要这么麻烦地用回调函数进行数据处理？
  假设现在使用 debug step 5步，假设用户程序在第三步时使用系统调用，从标准输入读取数据，那么这时用户程序会阻塞至读到用户输入未知，
  那么此时 dc.step(5)实际只能返回3步的寄存器信息，剩下2步的信息就被放在 socket 缓存区无人认领了

  我也想过将剩下的2步信息交给下一次操作，比如下一次 dc.step() 来返回。
  但是这太不合理了：实际在第3步用户在标准输入中输入数据后，用户程序就能跑完这5步，用户不用在 UI 上进行任何操作，寄存器窗口页就应该被刷新到完成5步后的状态
  整个过程就是异步的：不用管用户操作，寄存器窗口随 DOS 现实运行状况更新数值，无需用户干预

 */
public class DosConnector implements Runnable{
    private Gson gson = new Gson();
    private Process dos_process;
    private  Socket dos_client;
    private DosASMProcessFunction asm_func;
    private DosTraceProcessFunction trace_func;
    private DosStdProcessFunction std_func;

    /**
     * 在后台进程启动 dos,使用 socket 进行数据交换
     * dos 作为服务端，DosConnector 作为请求端
     * socket 的端口由 command_port 指定
     * @param command_port 指定与 dos 交换信息的 socket 的端口，dos 将在 command_port 开放服务
     * @throws IOException 无法连接 dos 会抛出 IOException, 可以尝试换一个端口重连
     */
    public DosConnector(int command_port) throws  IOException{
        /*
          初始化 dos, 连接 dos
         */

        initDOS(command_port);
        try {
            TimeUnit.SECONDS.sleep(5);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        dos_client = new Socket();
        dos_client.connect(new InetSocketAddress("localhost", command_port), 2);
        System.out.println("connected");
    }
    private void initDOS(int command_port) throws IOException{
        /*
        运行 dos-on-air 脚本来开机 dos
         */
        Runtime r = Runtime.getRuntime();
        // todo remove this
        File dos_path = new File("/Users/gexinjie/codes/dos-on-air");
        this.dos_process = r.exec(String.format("python3 dos_on_air.py localhost %d /Users/gexinjie/codes/dos-on-air", command_port) , null, dos_path);
    }

    static Pattern command_pat = Pattern.compile(".*?(\\{.*?\\})");

    /**
     * 对 buffer 检查 DOS 返回的运行结果，将每段运行结果分离，
     * 分离每段运行结果的依据是：每段运行结果是 {、} 包围的 json串
     * @param buffer
     * @return 运行结果的数组
     */
    private ArrayList<String> checkBuffer(ByteBuffer buffer) {
        String buf_string = new String(buffer.array(),0,  buffer.position());
        Matcher m = command_pat.matcher(buf_string);
        ArrayList<String> result = new ArrayList<>();
        while (m.find()) {
            result.add(m.group(1));
            buf_string =  buf_string.substring(m.end(), buf_string.length());
            m = command_pat.matcher(buf_string);
        }
        buffer.clear();
        return result;
    }

    /**
     * 解析 DOS 运行结果的函数
     */

    private DosASMOutput resolveASM(String json_string) {
        return gson.fromJson(json_string, DosASMOutput.class);
    }

    private DosTraceOutput resolveTrace(String json_string) {
        return gson.fromJson(json_string, DosTraceOutput.class);
    }

    private DosStdResult resolveStd(String json_string) {
        return gson.fromJson(json_string, DosStdResult.class);
    }

    /*
    注册处理 DOS 运行返回结果的方法
    注册的方法会在每次 DOS 返回运行结果时被调用
     */
    public void registerTraceFunc(DosTraceProcessFunction f) {
        this.trace_func = f;
    }

    public void registerASMFunc(DosASMProcessFunction f) {
        this.asm_func = f;
    }
    public void registerStdFunc(DosStdProcessFunction f) {
        this.std_func = f;
    }


    /*
    以下方法可以对dos 发送相应指令。都没有返回值，所有dos 状态都需要通过回调函数获得
    */
    public void startDebug(String exe_file) {
        assert  exe_file != null;
        String[] args = {exe_file};
        this.sendCommand("debug", args);
    }
    public void step(int n) {
        String[] args = {String.valueOf(n)};
        this.sendCommand("step", args);
    }
    public void getRegister() {
        this.sendCommand("show_register", null);
    }
    public void displayData(int from, int to) {
        String[] args = {String.valueOf(from), String.valueOf(to)};
        this.sendCommand("display_data", args);
    }
    public void masm(String asm_file) {
        String[] args = {asm_file};
        this.sendCommand("masm", args);
    }
    public void link(String obj_file) {
        String[] args = {obj_file};
        this.sendCommand("link", args);
    }
    private void sendCommand(String command, String[] args) {
        CommandJson cmd = new CommandJson(command, args);
        String cmd_json = this.gson.toJson(cmd);
        try {
            this.dos_client.getOutputStream().write(cmd_json.getBytes());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void socket_test(int command_port, int std_port) {
//        InetSocketAddress command_adress = new InetSocketAddress("localhost", command_port),
//                std_adress = new InetSocketAddress("localhost", std_port);
//        ByteBuffer buffer = ByteBuffer.allocate(1000);
//        try {
//            SocketChannel command_channel = SocketChannel.open(command_adress),
//                    std_channel = SocketChannel.open(std_adress);
//            System.out.println("connected");
//            command_channel.configureBlocking(false);
//            std_channel.configureBlocking(false);
//
//            Selector selector = Selector.open();
//            command_channel.register(selector, SelectionKey.OP_READ);
//            std_channel.register(selector, SelectionKey.OP_READ);
//            while (true) {
//                std_channel.read(buffer);
//
//
//            }
//
//        }catch (IOException e) {
//            e.printStackTrace();
//        }

    }
    public static void test_pat() {
        String input = "hkekjhekjhr{\"command\":\"hello\"}asdsd{\"command\":\"hello\"}";
        Matcher m = command_pat.matcher(input);
        while (m.find()) {
            System.out.println(m.group(1));
            input = input.substring(m.end(), input.length());
            m = command_pat.matcher(input);
        }
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            InputStream in_from_dos = this.dos_client.getInputStream();
            byte[] b = new byte[8192];
            while (true) {
                int n = in_from_dos.read(b);
                buffer.put(b, 0, n);
                ArrayList<String> outputs = checkBuffer(buffer);
                // todo callbacks
                for (String output :
                        outputs) {
                    if (output.contains("{\"stdout\":")) {
                        if (this.std_func != null) {
                            DosStdResult dos_result = resolveStd(output);
                            this.std_func.processStdOutput(dos_result.stdout);
                        }
                    }
                    else if (output.contains("\"AX\":") && output.contains("\"IP\":") &&
                            output.contains("\"flags\":")) {
                        if (this.trace_func != null) {
                            DosTraceOutput dos_result = resolveTrace(output);
                            this.trace_func.processDosOutput(dos_result);
                        }
                    }
                    else {
                        if (this.asm_func != null) {
                            DosASMOutput dos_result = resolveASM(output);
                            this.asm_func.processDosOutput(dos_result);
                        }
                    }
                }
                System.out.println(outputs);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    public void startListen() {
        System.out.println("start listening to dos");
        new Thread(this, "dos_listener").start();
    }

    public static void buffer_test() {
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        byte[] bs = {1, 2, 3, 4};
        byte[] bss = new byte[6];
        buffer.put(bs);
        buffer.flip();
        buffer.get(bss);
        System.out.println(Arrays.toString(bss));
    }

    public static void test_dos() {
        try {
            DosConnector dc = new DosConnector(12342);
            dc.startDebug("sample.exe");
            dc.step(3);
//            dc.startListen();
            try {
                ByteBuffer buffer = ByteBuffer.allocate(8192);
                InputStream in_from_dos = dc.dos_client.getInputStream();
                byte[] b = new byte[8192];
                while (true) {
//                    int n = in_from_dos.read(b);
//                    buffer.put(b, 0, n);
//                    ArrayList<String> outputs = dc.checkBuffer(buffer);
//                    // todo callbacks
//                    System.out.println(outputs);
//                    buffer.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Gson gson = new Gson();
//        CommandJson cmd = gson.fromJson("{\"command\":\"test\",\"args\":[\"test_args\"]}", CommandJson.class);
        CommandJson cmd = new CommandJson("test", null);
        System.out.println(gson.toJson(cmd));
//        test_pat();
        test_dos();
    }


}

class CommandJson {
    public String command = "debug";
    public String[] args = {"sample.exe"};

    public CommandJson(String command, String[] args) {
        this.command = command;
        this.args = args;
    }
}

class DosStdResult {
    public String stdout;
}
