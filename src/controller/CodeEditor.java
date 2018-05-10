package src.controller;

import javafx.scene.web.HTMLEditor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

public class CodeEditor extends HTMLEditor {
/*
    jsoup 这个神奇的包能够像jQuery一样解析HTML,修改html
    从而当用户输入各种内容时取出用户输入内容进行解析并再转换为语法高亮的形式
 */

    private boolean start_flag;
    private boolean is_modify;
    /**
     * 通过在FMXl上注册Controller和每个控件的fx：id
     * 然后再使用这个FXML notation能够将FXML上的各种控件连接到Controller上
     * 编写各种逻辑代码进行使用
     */
    private Thread printer = new Thread(() -> {
        File text_display = new File("test.html");
        while (start_flag) {
            String text = getHtmlText();
            try {
                Thread.sleep(500);

                if (text.equals(getHtmlText())) {
                    if (is_modify) {
                        System.out.println("lexer working");
                        Document content = Jsoup.parse(text);
                        Elements elements = content.body().children();
                        StringBuilder rawCode = new StringBuilder();
                        for (Element e : elements) {
                            rawCode.append(e.text()).append("\n");
                            Lexer.getInstance().generateToken(rawCode.toString());
                        }
                        System.out.println("token list result");
                        System.out.println(TokenManager.getInstance().toString());
                    }
                    is_modify = false;
                    continue;
                } else {
                    System.out.println("standby for editing stop");
                    is_modify = true;
                }


//               output html result
//                FileWriter write_in = new FileWriter(text_display, false);
//                write_in.write(text);
//                write_in.flush();
//                write_in.close();
                Thread.sleep(500);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    });

    public CodeEditor() {
        start_flag = true;
        is_modify = false;
        printer.start();
    }


    synchronized public void stop() {
        start_flag = false;

    }

}
