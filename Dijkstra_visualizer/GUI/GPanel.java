import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Comparator;

public class GPanel extends JPanel {

    private final Font FONT = new Font("TimesRoman", Font.BOLD, 14); // Шрифт надписей на вершинах
    private final int RADIUS = 20; // Радиус вершин
    private final Solver solver;    // Объект, выполняющий алгоритм
    private final ArrayList<VisualVertex> vertices = new ArrayList<>(); // Список вершин
    private final ArrayList<VisualEdge> edges = new ArrayList<>();  // Список рёбер
    private VisualVertex chosenVertex = null;   // Выбранная вершина
    private VisualVertex initVertex = null;     // Начальная вершина
    private VisualVertex lastConsideredVertex = null;   // Последняя рассмотренная вершина
    private VisualVertex draggedVertex = null;  // Перетаскиваемая вершина
    private VisualEdge chosenEdge = null;   // Выбранное ребро
    private VisualEdge drawnEdge = null;    // Рисуемое ребро
    boolean drawingEdge = false;    // Режим рисования ребра
    boolean holdingVertex = false;  // Вершина зажата/отпущена
    boolean draggingVertex = false; // Режим перетаскивания вершин
    private boolean isEditable = true;  // Режим редактирования графа
    private boolean choosingInit = false;   // Режим выбора начальной вершины

    public GPanel(Solver sol){
        super();

        this.solver = sol;

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(isEditable) {
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {  // Включение режима рисования ребра
                        drawingEdge = true;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if(isEditable) {
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {  // Выключение режима рисования ребра
                        drawingEdge = false;
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(isEditable) {
                    Window window = (Window) getTopLevelAncestor();
                    if ((e.getClickCount() == 2) && (e.getButton() == 1)) { // Добавление вершины
                        boolean intersects = false;
                        for (VisualVertex v : vertices) {
                            if ((Math.pow((e.getX() - v.getX()), 2) + Math.pow((e.getY() - v.getY()), 2)) <
                                    Math.pow(2 * (double) RADIUS, 2) + 10) {
                                intersects = true;
                                break;
                            }
                        }
                        if (!intersects && !(e.getX() < RADIUS + 10 || e.getY() < RADIUS + 10
                                || e.getX() > getSize().width - RADIUS - 10 || e.getY() > getSize().height - RADIUS - 10)) {
                            vertices.add(new VisualVertex(e.getX(), e.getY(), solver.addVertex()));
                            if (vertices.size() == 1) {
                                window.onGraphNotEmpty();
                            }
                        }
                    } else if ((e.getClickCount() == 1) && (e.getButton() == 1)) {  // Выбор вершины или ребра
                        window.onUncheck();
                        chosenVertex = chooseCircle(e.getX(), e.getY());
                        if (chosenVertex == null) {
                            ArrayList<VisualEdge> toChose = new ArrayList<>(5);
                            for (VisualEdge edge : edges) {
                                if (edge.getLine().contains(e.getX(), e.getY())) {
                                    toChose.add(edge);
                                }
                            }
                            double closeEnough = 0.0;
                            if (toChose.isEmpty()) {
                                chosenEdge = null;
                            }
                            for (VisualEdge edge : toChose) {
                                double curDistToPoint = Math.sqrt(Math.pow(e.getX() - edge.getV1().getX(), 2) + Math.pow(e.getY() - edge.getV1().getY(), 2))
                                        + Math.sqrt(Math.pow(e.getX() - edge.getV2().getX(), 2) + Math.pow(e.getY() - edge.getV2().getY(), 2));
                                double diff = curDistToPoint - Math.sqrt(Math.pow(edge.getV2().getX() - edge.getV1().getX(), 2) + Math.pow(edge.getV2().getY() - edge.getV1().getY(), 2));
                                if (closeEnough == 0.0) {
                                    closeEnough = diff;
                                    chosenEdge = edge;
                                } else if (diff < closeEnough) {
                                    closeEnough = diff;
                                    chosenEdge = edge;
                                }
                            }
                        } else {
                            chosenEdge = null;
                            window.onVertexChoice(chosenVertex.getId());
                        }
                        if (chosenEdge != null) {
                            window.onEdgeChoice(chosenEdge.getWeight());
                        }
                    }
                    getParent().repaint();
                }
                else if(choosingInit){
                    initVertex = chooseCircle(e.getX(), e.getY());
                    getParent().repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(isEditable) {
                    if (drawingEdge) {  // Начать рисование ребра
                        VisualVertex vertex = new VisualVertex(e.getX(), e.getY(), 0);
                        drawnEdge = new VisualEdge(vertex, vertex);
                        getParent().repaint();
                    }
                    draggedVertex = chooseCircle(e.getX(), e.getY());
                    if (draggedVertex != null)  // Обнаружить зажатую вершину
                        holdingVertex = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(isEditable) {
                    holdingVertex = false;  // Изначально зажатая вершина больше не берётся во внимание
                    if (drawingEdge && drawnEdge != null && (e.getX() != drawnEdge.getV1().getX() || e.getY() != drawnEdge.getV1().getY())) {   // Рисование ребра
                        boolean isInFirst = false;
                        boolean isInSecond = false;
                        VisualVertex first = null;
                        VisualVertex second = null;
                        for (VisualVertex v : vertices) {
                            if ((Math.pow((drawnEdge.getV1().getX() - v.getX()), 2) + Math.pow((drawnEdge.getV1().getY() - v.getY()), 2)) < RADIUS * RADIUS) {
                                first = v;
                                isInFirst = true;
                            }
                            if ((Math.pow((drawnEdge.getV2().getX() - v.getX()), 2) + Math.pow((drawnEdge.getV2().getY() - v.getY()), 2)) < RADIUS * RADIUS) {
                                second = v;
                                isInSecond = true;
                            }
                        }
                        if (isInFirst && isInSecond && first != second) {
                            if (first.getId() < second.getId()) {
                                drawnEdge.setV1(first);
                                drawnEdge.setV2(second);
                            } else {
                                drawnEdge.setV2(first);
                                drawnEdge.setV1(second);
                            }
                            boolean exists = false;
                            for (VisualEdge edge : edges) {
                                if (drawnEdge.equals(edge)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                setShape(drawnEdge);
                                edges.add(drawnEdge);
                                solver.addEdge(drawnEdge.getV1().getId(), drawnEdge.getV2().getId(), drawnEdge.getWeight());
                            }
                        }
                    }
                    drawnEdge = null;
                    getParent().repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e){
                super.mouseEntered(e);
                if(isEditable) {
                    setFocusable(true);
                    requestFocusInWindow();
                }
            }

            public void mouseExited(MouseEvent e){
                super.mouseExited(e);
                if(isEditable) {
                    drawingEdge = false;
                    setFocusable(false);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if(isEditable) {
                    if (drawingEdge && drawnEdge != null) {
                        drawnEdge.setV2(new VisualVertex(e.getX(), e.getY(), 0));   // Обновление рисуемого ребра
                    } else
                        drawnEdge = null;

                    if (holdingVertex && !drawingEdge) {    // Перетаскивание вершины
                        draggingVertex = false;
                        for (VisualEdge edge : edges) {
                            if (edge.getV1().getId() == draggedVertex.getId()) {
                                draggedVertex.setX(e.getX());
                                draggedVertex.setY(e.getY());
                                edge.setV1(draggedVertex);
                                setShape(edge);
                                draggingVertex = true;
                            }
                            if (edge.getV2().getId() == draggedVertex.getId()) {
                                draggedVertex.setX(e.getX());
                                draggedVertex.setY(e.getY());
                                edge.setV2(draggedVertex);
                                setShape(edge);
                                draggingVertex = true;
                            }
                        }
                        if (!draggingVertex) {
                            draggedVertex.setX(e.getX());
                            draggedVertex.setY(e.getY());
                        }
                    }
                    getParent().repaint();
                }
            }
        });
    }

    public void clear(){ // Удаление графа
        vertices.clear();
        edges.clear();
        chosenVertex = null;
        chosenEdge = null;
        initVertex = null;
        lastConsideredVertex = null;
        solver.clear();
        getParent().repaint();
    }

    public void uncheck(){  // Снять выбор со вершины или ребра
        this.chosenVertex = null;
        this.chosenEdge = null;
    }

    private VisualVertex chooseCircle(int x, int y) // Найти вершину на данной позиции
    {
        for (VisualVertex vertex : vertices) {
            if ((Math.pow((x - vertex.getX()), 2) + Math.pow((y - vertex.getY()), 2)) < RADIUS*RADIUS + 1) {
                return vertex;
            }
        }
        return null;
    }

    public void setChoosingInit(boolean flag){  // Режим выбора начальной вершины
        choosingInit = flag;
    }

    public void setEditable(boolean flag){  // Режим редактирования графа
        isEditable = flag;
    }

    public boolean start(){ // Начало работы алгоритма
        if(initVertex == null){
            return false;
        }
        solver.setInit(initVertex.getId());
        getParent().repaint();
        return true;
    }

    public void finish(){   // Завершение работы алгоритма
        initVertex = null;
        isEditable = true;
        getParent().repaint();
    }

    public void deleteVertex(){ // Удаление выбранной вершины
        if(chosenVertex != null) {
            int index = vertices.indexOf(chosenVertex);
            vertices.remove(chosenVertex);
            edges.removeIf(edge -> chosenVertex.getId() == edge.getV1().getId() || chosenVertex.getId() == edge.getV2().getId());
            for(int i = index; i < vertices.size(); i++){
                vertices.get(i).setId(i + 1);
            }
            solver.deleteVertex(chosenVertex.getId());
            chosenVertex = null;
            if(vertices.isEmpty()){
                Window window = (Window)getTopLevelAncestor();
                window.onGraphEmpty();
            }
            getParent().repaint();
        }
    }

    public void deleteEdge(){   // Удаление выбранного ребра
        if(chosenEdge != null){
            edges.remove(chosenEdge);
            solver.deleteEdge(chosenEdge.getV1().getId(), chosenEdge.getV2().getId());
            chosenEdge = null;
            getParent().repaint();
        }
    }

    private void setShape(VisualEdge edgeDrawn){    // Установка фигуры для отображения ребра
        int newWidth = (int)Math.sqrt((Math.pow(edgeDrawn.getV2().getX() - edgeDrawn.getV1().getX(),2))
                + (Math.pow(edgeDrawn.getV2().getY() - edgeDrawn.getV1().getY(),2)));
        int newX = (edgeDrawn.getV2().getX() + edgeDrawn.getV1().getX())/2 - newWidth/2;
        int newY = (edgeDrawn.getV2().getY() + edgeDrawn.getV1().getY())/2 - 1;
        int dX = edgeDrawn.getV2().getX() - edgeDrawn.getV1().getX();
        int dY = edgeDrawn.getV2().getY() - edgeDrawn.getV1().getY();
        Rectangle rect = new Rectangle(newX, newY, newWidth, 3);
        AffineTransform at = new AffineTransform();
        at.rotate(Math.atan((double)dY/dX), newX + (float)newWidth/2, newY + 1.5f);
        Shape shape = at.createTransformedShape(rect);

        edgeDrawn.setLine(shape);
    }

    public void load(){ // Загрузка из файла
        FileHandler fh = new FileHandler();
        ArrayList<Integer> loaded =  fh.load();
        if(!loaded.isEmpty()) {
            clear();
            int number = loaded.get(0);
            int vId = 1;
            for (int i = 0; i < number; i++) {
                vertices.add(new VisualVertex(loaded.get(vId + i*2), loaded.get(vId + i*2 + 1), solver.addVertex()));
            }
            for (int j = number*2 + 1; j < loaded.size(); j++) {
                if (loaded.get(j) == -1) {
                    vId++;
                } else {
                    int destVer = loaded.get(j++);
                    int weight = loaded.get(j);
                    solver.addEdge(vId, destVer, weight);
                    VisualEdge addedEdge = new VisualEdge(vertices.get(vId - 1), vertices.get(destVer - 1));
                    addedEdge.setWeight(weight);
                    setShape(addedEdge);
                    edges.add(addedEdge);
                }
            }
            Window window = (Window)getTopLevelAncestor();
            window.onGraphNotEmpty();
        }
    }

    public void save(){ // Сохранение в файл
        edges.sort(edgeComparator);
        FileHandler fh = new FileHandler();
        fh.save(vertices, edges);
    }

    // Компаратор для сортировки рёбер
    private static final Comparator<VisualEdge> edgeComparator = Comparator.comparingInt(o -> o.getV1().getId());

    public void setEdgeWeight(int w){   // Установка веса выбранного ребра
        chosenEdge.setWeight(w);
        solver.setEdgeWeight(chosenEdge.getV1().getId(), chosenEdge.getV2().getId(), w);
        getParent().repaint();
    }

    public void paintComponent(Graphics g2){    // Отрисовка графа
        Graphics2D g = (Graphics2D)g2;
        super.paintComponent(g);

        // Отрисовка рёбер графа с учётом текущего пути
        lastConsideredVertex = null;
        for(VisualVertex v : vertices){
            if(solver.getVertex(v.getId()).getColor() == Colors.COLOR2){
                lastConsideredVertex = v;
                break;
            }
        }
        ArrayList<Integer> path = new ArrayList<>();
        if(lastConsideredVertex != null){
            path.add(lastConsideredVertex.getId());
            Vertex par = solver.getVertex(lastConsideredVertex.getId()).getParent();
            while(par != null){
                path.add(0, par.getId());
                par = par.getParent();
            }
        }
        for(VisualEdge e : edges){
            g.setColor(Color.BLACK);
            if(path.contains(e.getV1().getId())){
                int ind = path.indexOf(e.getV1().getId());
                if((ind < path.size() - 1 && path.get(ind + 1) == e.getV2().getId()) || (ind > 0 && path.get(ind - 1) == e.getV2().getId())){
                    g.setColor(Color.RED);
                }
            }
            g.draw(e.getLine());
            g.fill(e.getLine());
            g.setColor(Color.BLACK);
            String weight = Integer.toString(e.getWeight());
            double tan = (double)(e.getV2().getY() - e.getV1().getY())/(e.getV2().getX() - e.getV1().getX());
            int xOffset = (e.getV1().getX() + e.getV2().getX())/2 - weight.length()*6 - 11;
            int yOffset;
            if(tan < 0){
                yOffset = (e.getV1().getY() + e.getV2().getY())/2 - 6;
            }
            else{
                yOffset = (e.getV1().getY() + e.getV2().getY())/2 + 14;// + weight.length()*10;
            }

            g.drawString(weight, xOffset, yOffset);
        }

        // Отрисовка выбранного ребра
        if(chosenEdge != null){
            g.setColor(Color.ORANGE);
            g.draw(chosenEdge.getLine());
            g.fill(chosenEdge.getLine());
        }

        // Отрисовка всех вершин
        for(VisualVertex p: vertices) {
            Color col = Color.BLACK;
            Colors color = solver.getVertex(p.getId()).getColor();
            int pathLen = solver.getVertex(p.getId()).getPathLen();
            String pLen;
            if(pathLen == Integer.MAX_VALUE){
                pLen = "\u221E";
            }
            else{
                pLen = Integer.toString(pathLen);
            }
            switch (color){
                case COLOR1 -> col = Color.WHITE;//Было GREEN
                case COLOR2 -> {
                    col = Color.PINK;
                    lastConsideredVertex = p;
                }
                case COLOR3 -> col = Color.ORANGE;
                case COLOR4 -> col = Color.lightGray;//Было BLUE
            }
            g.setColor(col);
            g.fillOval(p.getX() - RADIUS, p.getY() - RADIUS, RADIUS*2, RADIUS*2);
            g.setColor(Color.BLACK);
            g.drawOval(p.getX() - RADIUS, p.getY() - RADIUS, RADIUS*2, RADIUS*2);
            String s = Integer.toString(p.getId());
            g.setFont(FONT);
            g.drawString(s, p.getX() - 3 - 4*(s.length() - 1), p.getY() + 6);
            g.setColor(Color.WHITE);
            g.fillRect(p.getX() - 37 - 7*(pLen.length() - 1), p.getY() + 6, 8*(pLen.length() + 1), 14);
            g.setColor(Color.BLACK);
            g.drawRect(p.getX() - 37 - 7*(pLen.length() - 1), p.getY() + 6, 8*(pLen.length() + 1), 14);
            g.drawString(pLen, p.getX() - 35 - 7*(pLen.length() - 1), p.getY() + 18);
        }

        // Отрисовка рисуемого ребра
        if(drawnEdge != null){
            g.drawLine(drawnEdge.getV1().getX(), drawnEdge.getV1().getY(), drawnEdge.getV2().getX(), drawnEdge.getV2().getY());
        }

        // Отрисовка начальной вершины
        if(initVertex != null){
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(3));
            g.drawOval(initVertex.getX() - RADIUS, initVertex.getY() - RADIUS, RADIUS*2, RADIUS*2);
        }

        // Отрисовка выбранной вершины
        if(chosenVertex != null){
            g.setColor(Color.YELLOW);
            g.setStroke(new BasicStroke(3));
            g.drawOval(chosenVertex.getX() - RADIUS, chosenVertex.getY() - RADIUS, RADIUS*2, RADIUS*2);
        }
    }
}