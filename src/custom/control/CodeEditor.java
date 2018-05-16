package custom.control;

import javafx.concurrent.Task;
import javafx.scene.Parent;
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
    private boolean is_modify;
    private UIUpdater uiUpdater;

    //显示的文件
    private ProjectFile file;

    /**
     * 通过在FMXl上注册Controller和每个控件的fx：id
     * 然后再使用这个FXML notation能够将FXML上的各种控件连接到Controller上
     * 编写各种逻辑代码进行使用
     */
    private Thread printer = new Thread(() -> {

        while (start_flag) {
            String code_text = getHtmlText();
            try {
                Thread.sleep(500);
                if (code_text.equals(getHtmlText())) {
                    if (is_modify) {
                        synchronized (this) {
                            System.out.println("saving");
                            this.saveFile();
                            System.out.println("lexer working");
                            Document content = Jsoup.parse(code_text);
                            System.out.println("html res");
                            System.out.println(content.toString());
                            Elements elements = content.body().children();
                            StringBuilder rawCode = new StringBuilder();
                            for (Element e : elements) {
                                rawCode.append(e.text()).append("\n");
                            }
                            Lexer.getInstance().generateToken(rawCode.toString());
                            Parser.getInstance().parse();
                            System.out.println("token list result");
                            System.out.println(TokenManager.getInstance().toString());
                            System.out.println("start highlighting");
                            String highlight_res = SyntaxHighlighter.getInstance().startHighlighting();
                            System.out.println("highlighting result");
                            System.out.println(highlight_res);

                            uiUpdater.setResult(highlight_res);
                            uiUpdater.call();
                        }
                    } else {
                        continue;
                    }
                    is_modify = false;
                } else {
                    System.out.println("standby for editing stop");
                    is_modify = true;
                }
//               output html result
//                File text_display = new File("test.html");
//                FileWriter write_in = new FileWriter(text_display, false);
//                write_in.write(text);
//                write_in.flush();
//                write_in.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    });

    public CodeEditor(ProjectFile file) {
        start_flag = true;
        is_modify = false;
        uiUpdater = new UIUpdater(this);
        this.file = file;
        this.displayFile();
        uiUpdater.setOnSucceeded(event -> uiUpdater.editor.setHtmlText(uiUpdater.result));

        printer.start();
    }

    /**
     * 显示文件内容在HtmlEditor中
     */
    private void displayFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String temp;
            StringBuilder stringBuilder = new StringBuilder();
            while ((temp = reader.readLine()) != null) {
                stringBuilder.append("<p>");
                stringBuilder.append(temp);
                stringBuilder.append("</p>");
            }
            this.setHtmlText(stringBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String rawHtml = this.getHtmlText();
            Document doc = Jsoup.parse(rawHtml);
            for (Element e :
                    doc.getElementsByTag("p")) {
                writer.write(e.text());
            }
            writer.flush(); // 把缓存区内容压入文件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    synchronized public void stop() {
        start_flag = false;

    }

    class UIUpdater extends Task<String> {

        private CodeEditor editor;
        private String result;

        public UIUpdater(CodeEditor editor) {
            super();
            this.editor = editor;
        }

        public void setResult(String result) {
            this.result = result;
        }

        @Override
        protected String call() throws Exception {
            updateMessage("Succeeded");
            return result;
        }

    }
}
