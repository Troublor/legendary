package src.controller;

import javafx.concurrent.Task;
import javafx.scene.web.HTMLEditor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import src.model.Lexer;
import src.model.SyntaxHighlighter;
import src.model.TokenManager;

public class CodeEditor extends HTMLEditor {
/*
    jsoup 这个神奇的包能够像jQuery一样解析HTML,修改html
    从而当用户输入各种内容时取出用户输入内容进行解析并再转换为语法高亮的形式
 */

    private boolean start_flag;
    private boolean is_modify;
    private UIUpdater uiUpdater;
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

    public CodeEditor() {
        start_flag = true;
        is_modify = false;
        uiUpdater = new UIUpdater(this);
        uiUpdater.setOnSucceeded(event -> uiUpdater.editor.setHtmlText(uiUpdater.result));

        printer.start();
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
