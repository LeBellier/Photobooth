package x.leBellier.photobooth;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * A complete Java class that demonstrates how to create an HTML viewer with
 * styles, using the JEditorPane, HTMLEditorKit, StyleSheet, and JFrame.
 * 
 * @author alvin alexander, devdaily.com.
 *
 */
public class HtmlEditorKitTest extends JEditorPane {
    private static final long serialVersionUID = -5502363564300959381L;

    public HtmlEditorKitTest() {

	// make it read-only
	setEditable(false);

	// add an html editor kit
	HTMLEditorKit kit = new HTMLEditorKit();
	setEditorKit(kit);

	// add some styles to the html
	StyleSheet styleSheet = kit.getStyleSheet();
	styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
	styleSheet.addRule("h1 {color: blue;}");
	styleSheet.addRule("h2 {color: #ff0000;}");
	styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");

	// create some simple html as a string
	String htmlString = "<html>\n" + "<body>\n" + "<h1>Welcome!</h1>\n" + "<h2>This is an H2 header</h2>\n"
		+ "<p>This is some sample text</p>\n"
		+ "<p><a href=\"http://devdaily.com/blog/\">devdaily blog</a></p>\n" + "</body>\n";

	// create a document, set it on the jeditorpane, then add the html
	Document doc = kit.createDefaultDocument();
	setDocument(doc);
	setText(htmlString);
    }

    public void setHtml(String html) {
	setText(html);
    }

}