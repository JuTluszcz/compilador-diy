package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.FlowLayout;

// Importações para o controle de arquivos (supondo que estas classes existam)
import controleArquivo.CreateFile;
import controleArquivo.ReadFile;

public class Tela extends JFrame {

    // Elementos da Interface Gráfica
    private JMenuBar menuBar;
    private JMenu mnFile;
    private JMenuItem mntmNovo, mntmAbrirArquivo, mntmSalvar, mntmSalvarComo;
    private JButton btnNovo, btnAbrirArquivo, btnCompilador, btnSalvarComo;
    private JPanel panel_1, panel_2, panel_3;
    private JScrollPane scrollPane, scrollPane_1, scrollPane_2, scrollPane_3;
    private JTextPane textArquivo;
    private JTextArea textMsg, textConsole, textScroll;
    private StyledDocument doc;

    // Estruturas de Dados e Controle
    private HashMap<String, Style> styles = new HashMap<>();
    private Timer parseTimer;
    private File file;
    private String campoTexto = null;
    private boolean controleArquivo = false; // Controle de fluxo de compilação
    private int contEnter = 1; // Conta a quantidade de 'enter'

    // Listeners
    private btnNovoListener btnNovoL; // Listener para o botão "Novo"
    private btnAbrirListener btnAbrirL; // Listener para o botão "Abrir"

    /**
     * Construtor: configura a interface e seus componentes
     */
    public Tela() {
        lookFeel();

        btnNovoL = new btnNovoListener();
        btnAbrirL = new btnAbrirListener();

        setTitle("Compilador de Dungeons");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 618);

        // Painel de fundo com imagem customizada e layout responsivo (BorderLayout)
        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon background = new ImageIcon(getClass().getResource("/resources/fundo.jpeg"));
                // Desenha a imagem de fundo redimensionada
                g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        setContentPane(backgroundPanel);

        // ========= MENU =========
        menuBar = new JMenuBar();
        menuBar.setBackground(Color.DARK_GRAY);
        setJMenuBar(menuBar);

        mnFile = new JMenu("Arquivo");
        menuBar.add(mnFile);

        mntmNovo = new JMenuItem("Novo");
        mntmNovo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        mntmNovo.addActionListener(btnNovoL);
        mnFile.add(mntmNovo);

        mntmAbrirArquivo = new JMenuItem("Abrir Arquivo");
        mntmAbrirArquivo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        mntmAbrirArquivo.addActionListener(btnAbrirL);
        mnFile.add(mntmAbrirArquivo);

        mntmSalvar = new JMenuItem("Salvar");
        mntmSalvar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        mntmSalvar.setHorizontalAlignment(SwingConstants.LEFT);
        mntmSalvar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salvar();
            }
        });
        mnFile.add(mntmSalvar);

        mntmSalvarComo = new JMenuItem("Salvar Como...");
        mntmSalvarComo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salvarComo();
            }
        });
        mnFile.add(mntmSalvarComo);

        // ========= PAINEL SUPERIOR (Toolbar) =========
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.DARK_GRAY);

        btnNovo = new JButton(new ImageIcon(getClass().getResource("/resources/file.jpeg")));
        btnNovo.addActionListener(btnNovoL);
        btnNovo.setBackground(SystemColor.activeCaptionBorder);
        topPanel.add(btnNovo);

        btnAbrirArquivo = new JButton(new ImageIcon(getClass().getResource("/resources/grimore.jpeg")));
        btnAbrirArquivo.setBackground(Color.LIGHT_GRAY);
        btnAbrirArquivo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Se houver alterações, pergunta se deseja salvar
                if (!textArquivo.getText().equals("")) {
                    int resp = JOptionPane.showConfirmDialog(null, "Salvar alterações? Caso não salve o arquivo será perdido!!");
                    if (resp == 0) {
                        if (file == null) {
                            salvarComo();
                        } else {
                            salvar();
                        }
                    }
                }
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile().getAbsoluteFile();
                    ReadFile read = new ReadFile();
                    read.openFile(file);
                    textArquivo.setText("");
                    campoTexto = "";
                    campoTexto = read.readRecords();
                    try {
                        doc.insertString(doc.getLength(), campoTexto, doc.getStyle("default"));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                    controleArquivo = true;
                    textScroll.setText("");
                    contEnter = read.getContLinhaArquivo();
                    contEnter++;
                    textScroll.append(read.numLine());
                    read.closeFile();
                }
            }
        });
        topPanel.add(btnAbrirArquivo);

        JButton btnSalvar = new JButton(new ImageIcon(getClass().getResource("/resources/salvar.jpeg")));
        btnSalvar.setBackground(SystemColor.activeCaptionBorder);
        btnSalvar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salvar();
            }
        });
        topPanel.add(btnSalvar);

        btnSalvarComo = new JButton(new ImageIcon(getClass().getResource("/resources/salvarComo.jpeg")));
        btnSalvarComo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salvarComo();
            }
        });
        topPanel.add(btnSalvarComo);

        btnCompilador = new JButton(new ImageIcon(getClass().getResource("/resources/executar.jpeg")));
        btnCompilador.setBackground(Color.GREEN);
        topPanel.add(btnCompilador);

        // Opcional: definir tamanho preferencial para o topPanel
        topPanel.setPreferredSize(new Dimension(getWidth(), 50));
        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        // ========= PAINEL CENTRAL =========
        // --- Editor de Texto (panel_1) ---
        panel_1 = new JPanel(new BorderLayout());
        JLabel lblArquivo = new JLabel("Arquivo");
        panel_1.add(lblArquivo, BorderLayout.NORTH);

        // Painel interno para o editor (divide a área entre o número das linhas e o editor)
        JPanel editorPanel = new JPanel(new BorderLayout());

        // ScrollPane para os números das linhas (textScroll)
        scrollPane_3 = new JScrollPane();
        scrollPane_3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane_3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        textScroll = new JTextArea("1");
        textScroll.setFont(new Font("Consolas", Font.PLAIN, 15));
        textScroll.setEnabled(false);
        textScroll.setEditable(false);
        scrollPane_3.setViewportView(textScroll);

        // ScrollPane para o editor de texto (textArquivo)
        scrollPane_2 = new JScrollPane();
        textArquivo = new JTextPane();
        doc = textArquivo.getStyledDocument();
        textArquivo.setFont(new Font("Consolas", Font.PLAIN, 15));
        textArquivo.setToolTipText("Escreva sua aventura");
        textArquivo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!textArquivo.getText().trim().equals("")) {
                        contEnter++;
                        contaLinha(KeyEvent.VK_ENTER);
                    } else {
                        textScroll.setText("");
                        contEnter++;
                        for (int i = 1; i < contEnter; i++) {
                            textScroll.append(i + "\n");
                        }
                        textScroll.append(contEnter + "");
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (!textArquivo.getText().trim().equals("")) {
                        contEnter--;
                        contaLinha(KeyEvent.VK_BACK_SPACE);
                    } else {
                        textScroll.setText("");
                        contEnter--;
                        for (int i = 1; i < contEnter; i++) {
                            textScroll.append(i + "\n");
                        }
                        textScroll.append(contEnter + "");
                        if (contEnter < 1) {
                            contEnter = 1;
                            textScroll.setText("1");
                        }
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    Point point = scrollPane_2.getViewport().getViewPosition();
                    scrollPane_3.getViewport().setViewPosition(point);
                }
            }
        });
        scrollPane_2.setViewportView(textArquivo);

        // Adiciona o número das linhas e o editor ao painel interno
        editorPanel.add(scrollPane_3, BorderLayout.WEST);
        editorPanel.add(scrollPane_2, BorderLayout.CENTER);

        // Insere o editor interno (com números de linha) no panel_1
        panel_1.add(editorPanel, BorderLayout.CENTER);

        // --- Área "Árvore Sintática" (panel_3) ---
        panel_3 = new JPanel(new BorderLayout());
        JLabel lblMensagem = new JLabel("Arvore Sintática");
        panel_3.add(lblMensagem, BorderLayout.NORTH);
        scrollPane_1 = new JScrollPane();
        textMsg = new JTextArea();
        textMsg.setForeground(new Color(0, 128, 0));
        textMsg.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 15));
        textMsg.setEditable(false);
        scrollPane_1.setViewportView(textMsg);
        panel_3.add(scrollPane_1, BorderLayout.CENTER);

        // Cria o JSplitPane para dividir o editor e a área "Árvore Sintática"
        JSplitPane splitPaneCentro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel_1, panel_3);
        splitPaneCentro.setResizeWeight(0.7); // 70% do espaço para o editor (panel_1)
        // Define tamanhos mínimos para evitar encolhimento excessivo
        panel_1.setMinimumSize(new Dimension(600, 400));
        panel_3.setMinimumSize(new Dimension(300, 400));

        // ========= PAINEL INFERIOR (Console - panel_2) =========
        panel_2 = new JPanel(new BorderLayout());
        JLabel lblConsole = new JLabel("Console");
        panel_2.add(lblConsole, BorderLayout.NORTH);
        scrollPane = new JScrollPane();
        textConsole = new JTextArea();
        textConsole.setForeground(SystemColor.textHighlight);
        textConsole.setFont(new Font("Consolas", Font.BOLD | Font.ITALIC, 16));
        textConsole.setEditable(false);
        scrollPane.setViewportView(textConsole);
        panel_2.add(scrollPane, BorderLayout.CENTER);
        // Definindo um tamanho mínimo para o console (opcional)
        panel_2.setMinimumSize(new Dimension(100, 100));

        // ========= CRIA O JSPLITPANE VERTICAL =========
        // Agrupa a área central (splitPaneCentro) e o console (panel_2)
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneCentro, panel_2);
        // Define que, ao redimensionar, a área de cima ocupe 75% e a de baixo 25%
        verticalSplitPane.setResizeWeight(0.75);
        // Você pode definir também uma posição inicial para o divisor, por exemplo:
        // verticalSplitPane.setDividerLocation(500);

        // Adiciona o JSplitPane vertical na região central do backgroundPanel
        backgroundPanel.add(verticalSplitPane, BorderLayout.CENTER);

        // ========= CONFIGURAÇÃO DOS ESTILOS E PARSING DO DOCUMENTO =========
        setupStyles();

        parseTimer = new Timer(5, e -> parseDocument());
        parseTimer.setRepeats(false);

        textArquivo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parseTimer.restart();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                parseTimer.restart();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                parseTimer.restart();
            }
        });

        // Sincroniza a rolagem dos números de linha com o editor
        scrollPane_2.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                Point point = scrollPane_2.getViewport().getViewPosition();
                scrollPane_3.getViewport().setViewPosition(point);
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Método para configurar o Look & Feel
    public void lookFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()){
                if ("Nimbus".equals(info.getName())){
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Não foi possível executar o Nimbus");
        }
    }

    public JButton getCompilar() {
        return btnCompilador;
    }

    public JTextPane getTextArquivo(){
        return textArquivo;
    }

    public void setConsole(String msg){
        textConsole.append(msg);
    }

    public void setMsg(String msg){
        textMsg.append(msg);
    }

    public File getFile(){
        return file;
    }

    public void setFile(File file){
        this.file = file;
    }

    public String getCampoTexto(){
        return campoTexto;
    }

    public void setCampoTexto(String campoTexto){
        this.campoTexto = campoTexto;
    }

    public boolean getControleArquivo(){
        return controleArquivo;
    }

    public void setControleArquivo(boolean controle){
        this.controleArquivo = controle;
    }

    public void setVazioConsoleMsg(){
        textConsole.setText("");
        textMsg.setText("");
    }

    // Método para salvar o arquivo (se já existir)
    public void salvar(){
        campoTexto = "";
        controleArquivo = true;
        if (file != null) {
            CreateFile create = new CreateFile();
            create.openFile(file);
            create.addRecords(textArquivo.getText());
            create.closeFile();
            campoTexto = textArquivo.getText();
        } else {
            salvarComo();
        }
    }

    // Método para salvar como (novo arquivo)
    public void salvarComo(){
        JFileChooser jc = new JFileChooser();
        jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int i = jc.showSaveDialog(null);
        if (i != JFileChooser.CANCEL_OPTION) {
            file = jc.getSelectedFile();
            campoTexto = "";
            controleArquivo = true;
            CreateFile create = new CreateFile();
            create.openFile(file);
            create.addRecords(textArquivo.getText());
            create.closeFile();
            campoTexto = textArquivo.getText();
        }
    }

    // Método para contar as linhas do editor
    public void contaLinha(int key){
        String[] separa = textArquivo.getText().split("\n");
        textScroll.setText("");
        if (contEnter >= separa.length) {
            for (int i = 1; i < contEnter; i++){
                textScroll.append(i + "\n");
            }
            textScroll.append(contEnter + "");
        } else {
            for (int i = 1; i < separa.length; i++){
                textScroll.append(i + "\n");
            }
            textScroll.append(separa.length + "");
            if (key == KeyEvent.VK_ENTER)
                textScroll.append((separa.length + 1) + "");
            contEnter = separa.length;
        }
    }

    public JFrame getFrame() {
        return this;
    }

    public void setFrame(JFrame frame) {
        // Método opcional, se necessário
    }

    // Listener para o botão "Novo"
    class btnNovoListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!textArquivo.getText().equals("")) {
                int resp = JOptionPane.showConfirmDialog(null, "Salvar alterações? Caso não salve o arquivo será perdido!!");
                if (resp == 0) {
                    if (file == null) {
                        salvarComo();
                    } else {
                        salvar();
                    }
                    textArquivo.setText("");
                    textScroll.setText("1");
                    contEnter = 1;
                } else {
                    textArquivo.setText("");
                    textScroll.setText("1");
                    contEnter = 1;
                }
            } else {
                textArquivo.setText("");
                textScroll.setText("1");
                contEnter = 1;
            }
        }
    }

    // Listener para o botão "Abrir"
    class btnAbrirListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!textArquivo.getText().equals("")) {
                if (file == null) {
                    salvarComo();
                } else {
                    salvar();
                }
                textArquivo.setText("");
                textScroll.setText("1");
                contEnter = 1;
            } else {
                textArquivo.setText("");
                textScroll.setText("1");
                contEnter = 1;
            }
        }
    }

    // Configuração dos estilos usados no documento
    private void setupStyles() {
        Style defaultStyle = doc.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, Color.BLACK);

        Style keywordStyle = doc.addStyle("keyword", null);
        StyleConstants.setForeground(keywordStyle, Color.RED);
        StyleConstants.setBold(keywordStyle, true);
        styles.put("keyword", keywordStyle);

        Style mainStyle = doc.addStyle("main", null);
        StyleConstants.setForeground(mainStyle, new Color(255, 204, 51));
        StyleConstants.setItalic(mainStyle, true);
        styles.put("main", mainStyle);

        Style structureStyle = doc.addStyle("structure", null);
        StyleConstants.setForeground(structureStyle, Color.BLUE);
        StyleConstants.setItalic(structureStyle, true);
        styles.put("structure", structureStyle);
    }

    // Realiza o parsing do documento para aplicar os estilos (ex.: sintaxe)
    private void parseDocument() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    String text = doc.getText(0, doc.getLength());

                    // Padrões para palavras-chave e strings
                    String[] keywords = {"forca", "destreza", "inteligencia", "sabedoria"};
                    String stringPattern = "\"([^\"\\\\]|\\\\.)*\"";

                    // Aplica estilo para strings (lembre-se de criar o estilo "string", se desejar)
                    applyStyle(stringPattern, "string");

                    // Aplica estilo para palavras-chave
                    for (String keyword : keywords) {
                        applyStyle("\\b" + keyword + "\\b", "keyword");
                    }

                    String mainPattern = "campanha";
                    applyStyle(mainPattern, "main");

                    String structurePattern = "\\b(rola_um_dado_ai|acerta|erra|long_rest|iniciativa|sua_vez|vez_do_monstro)\\b";
                    applyStyle(structurePattern, "structure");

                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    // Aplica um estilo aos trechos do texto que casem com o padrão
    private void applyStyle(String pattern, String styleName) {
        try {
            String text = doc.getText(0, doc.getLength());
            Matcher matcher = Pattern.compile(pattern).matcher(text);
            Style style = styles.get(styleName);

            while (matcher.find()) {
                AttributeSet currentAttributes = doc.getCharacterElement(matcher.start()).getAttributes();
                if (!currentAttributes.isEqual(style)) {
                    doc.setCharacterAttributes(
                        matcher.start(),
                        matcher.end() - matcher.start(),
                        style,
                        false
                    );
                }
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }
}
