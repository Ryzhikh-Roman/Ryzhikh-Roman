import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class AutoMode extends Thread{
    private final GPanel canvas;    // Панель с графом
    private final JTextArea textArea;   // Область для записи данных
    private final JToggleButton autoButton; // Кнопка автоматического режима
    private final JButton stepButton;   // Кнопка пошагового режима
    private final Solver solver;    // Объект, выполняющий алгоритм
    private final CustomLogger logger;  // Объект, предоставляющий записанные данные
    private final int period;   // Временной интервал автоматического выполнения
    private boolean active = true;  // Режим работы/ожидания потока
    private boolean alive = true;   // Продолжение/завершение работы потока

    public AutoMode(GPanel canvas, JTextArea ta, JToggleButton ab, JButton sb, Solver sol, CustomLogger log, int period){
        super();
        this.solver = sol;
        this.logger = log;
        this.textArea = ta;
        this.canvas = canvas;
        this.autoButton = ab;
        this.stepButton = sb;
        this.period = period;
    }

    public void run() { // Выполнение шагов алгоритма с заданной задержкой
        try {
            boolean running = solver.step(logger);
            canvas.getParent().repaint();
            while (running) {
                synchronized (this){
                    if(!active){
                        wait();
                    }
                }
                if(!alive){
                    alive = true;
                    return;
                }
                textArea.append(logger.getNextMessage() +"\n\n");
                textArea.setCaretPosition(textArea.getDocument().getLength());
                try {
                    Thread.sleep(period);
                    synchronized (this){
                        if(!active){
                            wait();
                        }
                    }
                    if(!alive){
                        alive = true;
                        return;
                    }
                    running = solver.step(logger);
                    canvas.getParent().repaint();
                } catch (InterruptedException e) {
                    interrupt();
                }
            }
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        autoButton.setSelected(false);
        autoButton.setEnabled(false);
        stepButton.setEnabled(false);
        StringBuilder results = new StringBuilder();
        for(String s : solver.results()) {
            results.append(s).append("\n");
        }
        textArea.append("Итоги:\n" + results);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public void disable(){
        this.active = false;
    }

    public void enable(){
        this.active = true;
        synchronized (this) {
            notify();
        }
    }

    public void kill(){
        alive = false;
        enable();
    }
}

public class Window extends JFrame{
    private AutoMode thr = null; // Поток автоматического режима
    private final Solver solver = new Solver(); // Объект, выполняющий алгоритм
    private final  CustomLogger logger = new CustomLogger(); // Объект, предоставляющий записанные данные
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT); // Разделённая панель с регулировкой высоты компонентов
    private final JPanel annotationsPanel = new JPanel();   // Панель, содержащая область вывода данных
    private final GPanel canvasPanel = new GPanel(solver);  // Панель для отображения графа
    private final JPanel settingsPanel = new JPanel();  // Панель установок и взаимодействия с объектами графа
    private final JPanel bottomPanel = new JPanel();    // Нижняя панель с кнопками "Начать", "Сброс", "Очистить", "Закрыть"
    private final JMenuItem loadButton = new JMenuItem("Загрузить");    // Кнопка загрузки графа из файла
    private final JMenuItem saveButton = new JMenuItem("Сохранить");    // Кнопка сохранения графа в файл
    private final JTextArea textArea = new JTextArea(); // Область вывода данных
    private final JLabel infoLabel = new JLabel("Информация");  // Метка с информацией о предлагаемых действиях
    private final JTextField textField = new JTextField();  // Текстовое поле для ввода данных
    private final JButton approveButton = new JButton("Подтвердить"); // Кнопка подтверждения выбора начальной вершины
    private final JButton setTimeButton = new JButton("Задать интервал");   // Кнопка задания интервала авт. режима
    private final JButton setButton = new JButton("Применить"); // Кнопка установки хар-к графа (веса ребра)
    private final JButton deleteButton = new JButton("Удалить");    // Кнопка удаления вершины/ребра
    private final JToggleButton autoButton = new JToggleButton("Авто"); // Кнопка автоматического режима
    private final JButton stepButton = new JButton("Шаг");  // Кнопка пошагового режима
    private final JButton beginButton = new JButton("Начать");  // Кнопка, запускающая инициализацию алгоритма
    private final JButton resetButton = new JButton("Сброс");   // Кнопка сброса в режим редактирования
    private final JButton clearButton = new JButton("Очистить");    // Кнопка очистки панели с графом
    private final JButton closeButton = new JButton("Закрыть");

    public Window(){
        super();
        init();
    }

    private void init(){
        /*
        Ограничители для менеджера размещения GridBagLayout
        */

        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        /*
        Настройка компонентов 3 уровня (кнопок, текстовых полей и т.п.)
         */

        // Настройка области с выводом данных, добавление возможности её прокрутки
        textArea.setEditable(false);
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
        });
        JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(-1, 80));


        // Настройка метки с информацией о действиях
        infoLabel.setHorizontalAlignment(SwingConstants.LEFT);
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoLabel.setBackground(Color.WHITE);
        infoLabel.setOpaque(true);
        infoLabel.setMinimumSize(new Dimension(200, -1));

        // Настройка кнопки загрузки из файла
        loadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(loadButton.isEnabled()) {
                    canvasPanel.load();
                }
            }
        });

        //Настройка кнопки сохранения в файл
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(saveButton.isEnabled()) {
                    canvasPanel.save();
                }
            }
        });

        // Настройка кнопка "Начать"
        beginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(beginButton.isEnabled()) {
                    onUncheck();
                    canvasPanel.setEditable(false);
                    canvasPanel.uncheck();
                    canvasPanel.getParent().repaint();
                    loadButton.setEnabled(false);
                    saveButton.setEnabled(false);
                    beginButton.setEnabled(false);
                    resetButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    textField.setText("");
                    infoLabel.setText("<html>Введите временной интервал<br>(в миллисекундах)</html>");
                    textField.setVisible(true);
                    setTimeButton.setVisible(true);
                }
            }
        });

        // Настройка кнопки "Сброс"
        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(resetButton.isEnabled()) {
                    solver.reset();
                    if (thr != null && thr.isAlive()) {
                        thr.kill();
                    }
                    canvasPanel.finish();
                    canvasPanel.setChoosingInit(false);
                    textArea.setText("");
                    infoLabel.setText("Информация");
                    canvasPanel.getParent().repaint();
                    resetButton.setEnabled(false);
                    beginButton.setEnabled(true);
                    clearButton.setEnabled(true);
                    autoButton.setEnabled(false);
                    autoButton.setSelected(false);
                    stepButton.setEnabled(false);
                    saveButton.setEnabled(true);
                    loadButton.setEnabled(true);
                    textField.setText("");
                    textField.setVisible(false);
                    setTimeButton.setVisible(false);
                    approveButton.setVisible(false);
                }
            }
        });

        // Настройка кнопки "Задать интервал"
        setTimeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                try {
                    int period = Integer.parseInt(textField.getText());
                    if(period < 0){
                        infoLabel.setText("<html>Укажите<br>неотрицательное число</html>");
                    }
                    else {
                        if(thr == null) {
                            thr = new AutoMode(canvasPanel, textArea, autoButton, stepButton, solver, logger, period);
                        }
                        else if(thr.getState() != Thread.State.NEW){
                            thr.kill();
                            thr = new AutoMode(canvasPanel, textArea, autoButton, stepButton, solver, logger, period);
                        }
                        textField.setVisible(false);
                        setTimeButton.setVisible(false);
                        infoLabel.setText("Выберите начальную вершину");
                        canvasPanel.setChoosingInit(true);
                        approveButton.setVisible(true);
                    }
                }
                catch(NumberFormatException ex){
                    infoLabel.setText("<html>Неверный формат,<br>введите заново</html>");
                }
            }
        });

        // Настройка кнопки подтверждения выбора начальной вершины
        approveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (canvasPanel.start()){
                    canvasPanel.setChoosingInit(false);
                    approveButton.setVisible(false);
                    infoLabel.setText("Информация");
                    autoButton.setEnabled(true);
                    stepButton.setEnabled(true);
                }
                else{
                    infoLabel.setText("Вершина не выбрана");
                }
            }
        });

        // Настройка кнопка "Применить"
        setButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try{
                    int weight = Integer.parseInt(textField.getText());
                    if(weight < 1){
                        infoLabel.setText("<html>Ребро должно иметь<br>положительный вес!</html>");
                    }
                    else {
                        infoLabel.setText("<html>Задать вес ребра /<br>удалить ребро</html>");
                        canvasPanel.setEdgeWeight(weight);
                    }
                }
                catch (NumberFormatException ex){
                    infoLabel.setText("<html>Неверный формат,<br>введите заново</html>");
                }
            }
        });

        // Настройка конпки "Удалить"
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                canvasPanel.deleteVertex();
                canvasPanel.deleteEdge();
                infoLabel.setText("Информация");
                deleteButton.setVisible(false);
                textField.setVisible(false);
                setButton.setVisible(false);
            }
        });

        // Настройка кнопки автоматического режима
        autoButton.addItemListener(e -> {
            if(autoButton.isEnabled()) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    stepButton.setEnabled(false);
                    if (thr.isAlive()) {
                        thr.enable();
                    } else {
                        thr.start();
                    }
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    thr.disable();
                    stepButton.setEnabled(true);
                }
            }
        });

        // Настройка кнопки пошагового режима
        stepButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (stepButton.isEnabled()) {
                    boolean running = solver.step(logger);
                    if (!running) {
                        if (thr != null && thr.isAlive()) {
                            thr.kill();
                        }
                        StringBuilder results = new StringBuilder();
                        for (String s : solver.results()) {
                            results.append(s).append("\n\n");
                        }
                        textArea.append("Итоги:\n" + results);
                        autoButton.setEnabled(false);
                        stepButton.setEnabled(false);
                    } else {
                        textArea.append(logger.getNextMessage() + "\n");
                    }
                }
                canvasPanel.getParent().repaint();
            }
        });

        // Настройка кнопки "Очистить"
        clearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(clearButton.isEnabled()) {
                    super.mouseReleased(e);
                    onUncheck();
                    onGraphEmpty();
                    solver.clear();
                    canvasPanel.clear();
                }
            }
        });

        // Настройка кноки "Закрыть"
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(thr != null && thr.isAlive()){
                    thr.kill();
                }
                dispose();
            }
        });

        /*
        Настройка компонентов 2 уровня (панелей)
         */

        // Помещение прокручиваемой области для вывода данных на панель "Аннотации"
        annotationsPanel.setLayout(new GridBagLayout());
        annotationsPanel.setBackground(new Color(223,163,159));
        annotationsPanel.setBorder(BorderFactory.createTitledBorder("Аннотации"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1f;
        gbc.weighty = 1f;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        annotationsPanel.add(scrollPane, gbc);

        // Создание верхней панели меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");

        // Настройка кнопки "Закрыть" в меню
        JMenuItem closeMenuButton = new JMenuItem("Закрыть");
        closeMenuButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(thr != null && thr.isAlive()){
                    thr.kill();
                }
                dispose();
                super.mouseReleased(e);
            }
        });

        fileMenu.add(loadButton);
        fileMenu.add(saveButton);
        fileMenu.add(closeMenuButton);
        menuBar.add(fileMenu);

        // Настройка панели отображения графа
        canvasPanel.setMinimumSize(new Dimension(-1, 100));
        canvasPanel.setBorder(BorderFactory.createTitledBorder("Граф"));
        canvasPanel.setBackground(new Color(151,248,255));
        canvasPanel.setOpaque(true);

        // Добавление компонентов на разделённую панель
        splitPane.setTopComponent(annotationsPanel);
        splitPane.setBottomComponent(canvasPanel);
        splitPane.setDividerLocation(105);

        // Настройка панели установок
        settingsPanel.setLayout(new GridBagLayout());
        settingsPanel.setBackground(new Color(222,227,119));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Установки"));
        settingsPanel.setPreferredSize(new Dimension(200, -1));

        // Добавление элементов на панель установок
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1f;
        gbc.weighty = 0.005;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        settingsPanel.add(infoLabel, gbc);
        gbc.gridy = 1;
        settingsPanel.add(textField, gbc);
        gbc.gridy = 2;
        settingsPanel.add(approveButton, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0.4f;
        settingsPanel.add(setButton, gbc);
        gbc.gridx = 1;
        settingsPanel.add(deleteButton, gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1f;
        settingsPanel.add(setTimeButton, gbc);
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weighty = 0.495;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weightx = 0.4f;
        settingsPanel.add(autoButton, gbc);
        gbc.gridx = 1;
        settingsPanel.add(stepButton, gbc);

        // Настройка нижней панели
        bottomPanel.setLayout(new GridBagLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        bottomPanel.setBackground(new Color(140, 223, 122));

        // Добавление элементов на нижнюю панель
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.02f;
        gbc.weighty = 0f;
        bottomPanel.add(beginButton, gbc);
        gbc.gridx = 1;
        bottomPanel.add(resetButton, gbc);
        gbc.gridx = 2;
        gbc.weightx = 1f;
        bottomPanel.add(Box.createHorizontalStrut(0), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.02f;
        bottomPanel.add(clearButton, gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.02f;
        bottomPanel.add(closeButton, gbc);

        /*
        Настройка компонентов 1 уровня (главная панель)
         */

        getContentPane().setBackground(Color.GRAY);

        // Добавление меню на окно
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1f;
        gbc.weighty = 0f;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(menuBar, gbc);

        // Добавление разделённой панели (Аннотации/Граф) на окно
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1f;
        gbc.weighty = 1f;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(3,3,3,0);
        getContentPane().add(splitPane, gbc);

        // Добавление панели установок на окно
        gbc.gridx = 1;
        gbc.weightx = 0f;
        gbc.gridheight = 2;
        gbc.insets.set(3,3,3,3);
        getContentPane().add(settingsPanel, gbc);

        // Добавление нижней панели на окно
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1f;
        gbc.weighty = 0f;
        gbc.gridheight = 1;
        gbc.insets.set(0,3,3,0);
        getContentPane().add(bottomPanel, gbc);

        /*
        Настройка компонента 0 уровня (окна)
         */

        setTitle("Алгоритм Дейкстры");
        setMinimumSize(new Dimension(700,700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();

        saveButton.setEnabled(false);
        beginButton.setEnabled(false);
        resetButton.setEnabled(false);
        clearButton.setEnabled(false);
        textField.setVisible(false);
        approveButton.setVisible(false);
        setTimeButton.setVisible(false);
        setButton.setVisible(false);
        deleteButton.setVisible(false);
        autoButton.setEnabled(false);
        stepButton.setEnabled(false);

        setVisible(true);
    }

    // При выборе вершины
    public void onVertexChoice(int id){
        infoLabel.setText("Выбрана вершина " + id);
        deleteButton.setVisible(true);
    }

    // При выборе ребра
    public void onEdgeChoice(int weight){
        infoLabel.setText("<html>Задать вес ребра /<br>удалить ребро</html>");
        textField.setText(Integer.toString(weight));
        textField.setVisible(true);
        deleteButton.setVisible(true);
        setButton.setVisible(true);
    }

    // При снятии выбора
    public void onUncheck(){
        infoLabel.setText("Информация");
        textField.setVisible(false);
        setButton.setVisible(false);
        deleteButton.setVisible(false);
    }

    // Если граф содержит хотя бы одну вершину
    public void onGraphNotEmpty(){
        saveButton.setEnabled(true);
        beginButton.setEnabled(true);
        clearButton.setEnabled(true);
    }

    // Если из графа удалены все вершины
    public void onGraphEmpty(){
        saveButton.setEnabled(false);
        beginButton.setEnabled(false);
        clearButton.setEnabled(false);
    }

    public static void main(String[] args){
        new Window();
    }
}
