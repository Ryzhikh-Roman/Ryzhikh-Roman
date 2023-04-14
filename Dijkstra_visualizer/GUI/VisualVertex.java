// Вершина для отрисовки
public class VisualVertex {
    private int x;
    private int y;
    private int id;

    public VisualVertex(int x, int y, int id){
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public int getId(){
        return this.id;
    }
}
