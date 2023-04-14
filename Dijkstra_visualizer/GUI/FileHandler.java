import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FileHandler {
    private File file;

    public FileHandler(){
        file = null;
    }

    public void save(ArrayList<VisualVertex> vertices, ArrayList<VisualEdge> edges) { // Сохранение в файл
        JFileChooser chooser = new JFileChooser();
        chooser.showDialog(null, "Сохранить");
        file = chooser.getSelectedFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(vertices.size() + "\n");
            for(VisualVertex v : vertices){
                fw.write(v.getX() + " " + v.getY() + "\n");
            }
            int vId = 1;
            for(VisualEdge e : edges){
                while(e.getV1().getId() != vId){
                    vId++;
                    fw.write("/\n");
                }
                fw.write(e.getV2().getId() + " " + e.getWeight() + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<Integer> load(){   // Загрузка из файла
        JFileChooser chooser = new JFileChooser();
        chooser.showDialog(null, "Загрузить");
        file = chooser.getSelectedFile();
        ArrayList<Integer> input = new ArrayList<>();
        try{
            Scanner scanner = new Scanner(file);
            String next = scanner.next();
            while(scanner.hasNext()){
                if(next.equals("/")){
                   input.add(-1);
                }
                else{
                    input.add(Integer.parseInt(next));
                }
                next = scanner.next();
            }
            if(next.equals("/")){
                input.add(-1);
            }
            else{
                input.add(Integer.parseInt(next));
            }
        }
        catch(FileNotFoundException ex){
            ex.printStackTrace();
        }
        return input;
    }
}
