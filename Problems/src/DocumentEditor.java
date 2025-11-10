import java.util.ArrayList;
import java.util.List;

interface DocumentElement{
    public abstract  String render();
}

class TextElement implements DocumentElement{
    private String text;
    public TextElement(String text){
        this.text = text;
    }
    @Override
    public String render() {
        return text;
    }
}

class ImageElement implements DocumentElement{
    private String image;
    public ImageElement(String image){
        this.image = image;
    }
    @Override
    public String render() {
        return "IMAGE PATH: "+image;
    }
}

class Document{
    List<DocumentElement> elements;
    public Document(){
        this.elements = new ArrayList<>();
    }

    public void addElement(DocumentElement element){
        this.elements.add(element);
    }
    public String render(){
        String result = "";
        for(DocumentElement element : elements){
            result += element.render();
        }
        return result;
    }
}

interface Persistence{
    void save();
}

class FilePersistence implements Persistence{
    @Override
    public void save() {
        System.out.println("Saving file To Folder...");
    }
}

class DBPersistence implements Persistence{
    @Override
    public void save() {
        System.out.println("Saving file To Database...");
    }
}

class DocumentManager{
    Document document;
    Persistence persistence;
    String renderedDocument = "";
    public DocumentManager(Document document, Persistence persistence){
        this.document = document;
        this.persistence = persistence;
    }

    public void addText(String text){
        document.addElement(new TextElement(text));
    }

    public void addImage(String image){
        document.addElement(new ImageElement(image));
    }

    public void renderDocument(){
        this.renderedDocument = document.render();
        System.out.println("Rendered Document: "+renderedDocument);
    }

    public void save(){
        persistence.save();
    }
}
public class DocumentEditor {
    public static void main(String[] args) {
        Document document = new Document();
        Persistence persistence = new FilePersistence();

        DocumentManager documentManager = new DocumentManager(document, persistence);
        documentManager.addText("This is first line");
        documentManager.addText("This is second line");
        documentManager.addText("This is third line");
        documentManager.addImage("mypp.jpg");
        documentManager.renderDocument();
        documentManager.save();
    }

}
