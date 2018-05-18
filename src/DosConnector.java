package src;

import com.google.gson.Gson;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

public class DosConnector implements Runnable{
    private Gson gson = new Gson();
    private Process dos_process;
    private  Socket dos_client;
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
    public static void test_pat() {
        String input = "hkekjhekjhr{\"command\":\"hello\"}asdsd{\"command\":\"hello\"}";
        Matcher m = command_pat.matcher(input);
        while (m.find()) {
            System.out.println(m.group(1));
            input = input.substring(m.end(), input.length());
            m = command_pat.matcher(input);
        }
    }
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
                    int n = in_from_dos.read(b);
                    buffer.put(b, 0, n);
                    ArrayList<String> outputs = dc.checkBuffer(buffer);
                    // todo callbacks
                    System.out.println(outputs);
                    buffer.clear();
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
