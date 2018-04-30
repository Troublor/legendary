package src.sample;

import javafx.fxml.FXML;
import javafx.scene.web.HTMLEditor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
/*
    jsoup 这个神奇的包能够像jQuery一样解析HTML,修改html
    从而当用户输入各种内容时取出用户输入内容进行解析并再转换为语法高亮的形式
 */

public class Controller {
    /**
     * 通过在FMXl上注册Controller和每个控件的fx：id
     * 然后再使用这个FXML notation能够将FXML上的各种控件连接到Controller上
     * 编写各种逻辑代码进行使用
     */
    @FXML
    private HTMLEditor editor;
    private Thread printer = new Thread(new Runnable() {
        @Override
        public void run() {
            File text_display = new File("test.html");

            while (true) {
                if (editor == null)
                    continue;
                try {
                    System.out.println("update text");
                    String text = editor.getHtmlText();
                    Document content = Jsoup.parse(text);
                    content.body();
                    System.out.println(content.body());
                    FileWriter write_in = new FileWriter(text_display, false);
                    write_in.write(text);
                    write_in.flush();
                    write_in.close();
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
    });

    public Controller() {
        printer.start();
    }
}
