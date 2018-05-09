package src.controller;

import javafx.scene.web.HTMLEditor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;

public class CodeEditor extends HTMLEditor {
/*
    jsoup 这个神奇的包能够像jQuery一样解析HTML,修改html
    从而当用户输入各种内容时取出用户输入内容进行解析并再转换为语法高亮的形式
 */

    private boolean start_flag;
    /**
     * 通过在FMXl上注册Controller和每个控件的fx：id
     * 然后再使用这个FXML notation能够将FXML上的各种控件连接到Controller上
     * 编写各种逻辑代码进行使用
     */
    private Thread printer = new Thread(() -> {
        File text_display = new File("test.html");
        String text = "";
        while (start_flag) {

            try {
                if (text.equals(getHtmlText())) {
                    Thread.sleep(500);
                    continue;
                }
                text = getHtmlText();
                System.out.println("update text");
                Document content = Jsoup.parse(text);
                content.body();
                System.out.println(content.body());
                FileWriter write_in = new FileWriter(text_display, false);
                write_in.write(text);
                write_in.flush();
                write_in.close();
                Thread.sleep(500);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    });

    public CodeEditor() {
        start_flag = true;
        printer.start();
    }


    synchronized public void stop() {
        start_flag = false;

    }

}
