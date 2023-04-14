import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

// Исполнитель алгоритма
public class Solver {
    private Vertex init;    // Начальная вершина
    private Vertex prev;    // Предыдущая рассмотренная вершина
    private final ArrayList<Vertex> vertSet;    // Вершины графа
    private final PriorityQueue<Vertex> front;  // Досягаемые на данный момент вершины

    public Solver() {
        this.init = null;
        this.prev = null;
        this.vertSet = new ArrayList<>();
        this.front = new PriorityQueue<>(1, distComp);
    }

    // Компаратор для вершин, определяет, какая из досягаемых выбирается на текущем шаге
    private final static Comparator<Vertex> distComp = (o1, o2) -> {
        if(o1.getPathLen() == o2.getPathLen()){
            return o1.getId() - o2.getId();
        }
        return o1.getPathLen() - o2.getPathLen();
    };

    // Добавить вершину
    public int addVertex() {
        this.vertSet.add(new Vertex(vertSet.size() + 1));
        return this.vertSet.size();
    }

    // Удалить вершину с указанным ID
    public void deleteVertex(int id) {
        Vertex toDel = this.vertSet.get(id - 1);
        for (Map.Entry<Vertex, Integer> pair : toDel.getAdjList().entrySet()) {
            pair.getKey().getAdjList().remove(toDel);
        }
        this.vertSet.remove(toDel);
        for (int i = id; i < this.vertSet.size() + 1; i++) {
            this.vertSet.get(i - 1).setId(i);
        }
    }

    // Получить вершину с указанным ID
    public Vertex getVertex(int id) {
        return this.vertSet.get(id - 1);
    }

    // Добавить ребро
    public void addEdge(int from, int to, int dist){
        Vertex fromVer = this.vertSet.get(from - 1);
        Vertex toVer = this.vertSet.get(to - 1);
        if(!fromVer.getAdjList().containsKey(toVer)) {
            fromVer.addToAdjList(toVer, dist);
            toVer.addToAdjList(fromVer, dist);
        }
    }

    // Установить вес указанного ребра
    public void setEdgeWeight(int from, int to, int weight){
        Vertex s = getVertex(from);
        Vertex d = getVertex(to);
        if(d != null && s != null && s.getAdjList().containsKey(d)) {
            s.getAdjList().replace(d, weight);
            d.getAdjList().replace(s, weight);
        }
    }

    // Удалить указанное ребро
    public void deleteEdge(int from, int to){
        Vertex fromVer = this.vertSet.get(from - 1);
        Vertex toVer = this.vertSet.get(to - 1);
        fromVer.getAdjList().remove(toVer);
        toVer.getAdjList().remove(fromVer);
    }

    // Назначить начальную вершину
    public void setInit(int init){
        this.init = this.vertSet.get(init - 1);
        this.front.add(this.init);
    }

    // Выполнение одного шага алгоритма
    public boolean step(CustomLogger logger){
        if(prev != null){
            prev.setColor(Colors.COLOR4);
        }
        if(!(front.isEmpty())){
            Vertex current = front.poll();
            current.setColor(Colors.COLOR2);
            prev = current;
            if(current == init){
                current.setPathLen(0);
            }
            logger.addMessage(formInfo(current));

            // Проверка на начличие путей из начальной вершины
            if (current.getAdjList().isEmpty()) {
                logger.addMessage("Начальная вершина не имеет смежных\n");
            }
            else {
                logger.addMessage("Рассматриваются смежные вершины: \n");
            }
            for(Map.Entry<Vertex, Integer> next : current.getAdjList().entrySet()) {

                // Проверка на факт рассмотрения вершины ранее
                if (next.getKey().getColor() == Colors.COLOR4) {
                    logger.addMessage("--Вершина " + next.getKey().getId() + " уже рассмотрена!\n");
                }
                // Проверка на изменение текущей длины пути до данной вершины
                if ((next.getKey().getColor() != Colors.COLOR4) &&
                        (current.getPathLen() + next.getValue() < next.getKey().getPathLen())) {
                        logger.addMessage("--Длина пути для вершины " + next.getKey().getId() + " меняется c " +
                                (next.getKey().getPathLen()==Integer.MAX_VALUE?"\u221E":next.getKey().getPathLen())
                                + " на " + (current.getPathLen() + next.getValue()) + "\n");
                    next.getKey().setParent(current);
                    next.getKey().setPathLen(current.getPathLen() + next.getValue());
                    if (!(front.contains(next.getKey()))) {
                        next.getKey().setColor(Colors.COLOR3);
                        front.add(next.getKey());
                    }
                }
                else if ((next.getKey().getColor() != Colors.COLOR4) &&
                        (current.getPathLen() + next.getValue() >= next.getKey().getPathLen())) {
                    logger.addMessage("--Для вершины " + next.getKey().getId() + " длина пути остается прежней\n");
                }
            }
            return true;
        }
        return false;
    }

    // Очистка графа
    public void clear(){
        this.init = null;
        this.prev = null;
        vertSet.clear();
        front.clear();
    }

    // Сброс графа до начального состояния
    public void reset(){
        this.init = null;
        this.prev = null;
        for(Vertex v : vertSet){
            v.setPathLen(Integer.MAX_VALUE);
            v.setParent(null);
            v.setColor(Colors.COLOR1);
        }
        front.clear();
    }

    // Форматирование информации о вершине
    private String formInfo(Vertex v){
        String info = v.getId() + ". ";
        StringBuilder path = new StringBuilder();
        Vertex par = v.getParent();
        if(par == null && !(v == init)){
            path = new StringBuilder("Путь: не существует\n");
            info = info + path + "Длина пути: " + "\u221E";
        }
        else {
            while (!(par == null)) {
                path.insert(0, par.getId() + " ");
                par = par.getParent();
            }
            path = new StringBuilder("Путь: " + path + v.getId() + "\n");
            info = info + path + "Длина пути: " + v.getPathLen() + "\n";
        }
        return info;
    }

    // Получение конечных результатов
    public ArrayList<String> results(){
        ArrayList<String> res = new ArrayList<>(0);
        for(Vertex v : this.vertSet){
            res.add(formInfo(v));
        }
        return res;
    }
}
