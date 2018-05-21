package custom.control;

import dfa.InvalidTransformationException;
import javafx.application.Platform;
import javafx.scene.web.HTMLEditor;
import model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class CodeEditor extends HTMLEditor {
/*
    jsoup 这个神奇的包能够像jQuery一样解析HTML,修改html
    从而当用户输入各种内容时取出用户输入内容进行解析并再转换为语法高亮的形式
 */

    private boolean start_flag;
    private boolean is_modifying;

    //显示的文件
    private ProjectFile file;

    /**
     * 通过在FMXl上注册Controller和每个控件的fx：id
     * 然后再使用这个FXML notation能够将FXML上的各种控件连接到Controller上
     * 编写各种逻辑代码进行使用
     */
    private Thread printer = new Thread(() -> {
        boolean is_highlighted = false;
        while (start_flag) {
            try {

                Thread.sleep(100);
                String code_text = getHtmlText();
                Thread.sleep(400);
                if (code_text.equals(getHtmlText())) {
                    if (!is_modifying && !is_highlighted) {
                        startHighlight();
                        is_highlighted = true;
                    }
                    is_modifying = false;
                } else {
                    is_modifying = true;
                    is_highlighted = false;
                    System.out.println("standby for editing stop");
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    });

    public CodeEditor(ProjectFile file) {
        start_flag = true;
        is_modifying = false;
        this.file = file;
        this.displayFile();

        printer.start();
    }

    private void startHighlight() throws InvalidTransformationException {
        synchronized (this) {
            System.out.println("lexer working");
            Document content = Jsoup.parse(getHtmlText());
            System.out.println("html res");
            System.out.println(content.toString());
            Elements elements = content.body().children();
            StringBuilder rawCode = new StringBuilder();
            if (elements.size() == 0 && !content.body().text().equals(""))
                rawCode.append(content.body().text()).append("\n");
            for (Element e : elements) {
                rawCode.append(e.text()).append("\n");
            }
            Lexer.getInstance().generateToken(rawCode.toString());
            Parser.getInstance().parse();
            System.out.println("token list result");
            System.out.println(TokenManager.getInstance().toString());
            System.out.println("start highlighting");
            String code_text = SyntaxHighlighter.getInstance().startHighlighting();
            System.out.println("highlighting result");
            System.out.println(code_text);
            final String highlight_res = code_text;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setHtmlText(highlight_res);
                }
            });
            this.saveFile();
            System.out.println("saving");
        }
    }

    /**
     * 显示文件内容在HtmlEditor中
     */
    private void displayFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String temp;
            Document d = Jsoup.parse(getHtmlText());
            while ((temp = reader.readLine()) != null) {
                d.body().appendElement("p").appendText(temp);
            }

            this.setHtmlText(d.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String rawHtml = this.getHtmlText();
            Document doc = Jsoup.parse(rawHtml);
            for (Element e : doc.getElementsByTag("p")) {
                writer.write(e.text() + "\n");
            }
            writer.flush(); // 把缓存区内容压入文件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    synchronized public void stop() {
        start_flag = false;

    }

}
