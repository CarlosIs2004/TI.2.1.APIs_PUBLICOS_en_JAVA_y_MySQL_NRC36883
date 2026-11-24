package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisController;
import ec.edu.monster.modelo.Pais;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class FrmPais extends JFrame {

    private final PaisController controller = new PaisController();

    // ---- Paleta de colores ----
    private static final Color COLOR_HEADER  = new Color(0x2C3E50);
    private static final Color COLOR_PRIMARY = new Color(0x2980B9);
    private static final Color COLOR_SUCCESS = new Color(0x27AE60);
    private static final Color COLOR_DANGER  = new Color(0xC0392B);
    private static final Color COLOR_NEUTRAL = new Color(0x7F8C8D);
    private static final Color COLOR_FONDO   = new Color(0xECEFF1);
    private static final Color COLOR_BORDE   = new Color(0xCFD8DC);
    private static final Color COLOR_FILA_ALT = new Color(0xF2F6F9);

    // ---- Fuentes ----
    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FUENTE_BOLD   = new Font("Segoe UI", Font.BOLD, 13);

    // ---- Componentes ----
    private JButton btnImportarTodo, btnVaciar, btnGuardar, btnActualizar, btnEliminar, btnLimpiar, btnReporte;
    private JComboBox<String> comboRegiones;
    private JTable tblPaises;
    private JTextField txtId, txtNombre, txtCapital, txtRegion, txtPoblacion, txtMoneda;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JLabel lblEstado;

    // Consulta que alimenta la tabla ahora mismo (todos, filtro de región o búsqueda).
    private Supplier<List<Pais>> vistaActual = () -> controller.listarPaises();

    public FrmPais() {
        initUI();
        cargarTabla(controller.listarPaises());
    }

    private void initUI() {
        setTitle("Gestor de Países  -  API Pública (REST Countries v5) + MySQL");
        setSize(1000, 720);
        setMinimumSize(new Dimension(900, 580));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);

        add(crearCabecera(), BorderLayout.NORTH);
        add(crearPanelFormulario(), BorderLayout.WEST);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);

        registrarEventos();
    }

    // ====================== CABECERA ======================
    private JComponent crearCabecera() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel titulo = new JLabel("Gestor de Países - Monster");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        header.add(titulo, BorderLayout.WEST);
        
        return header;
    }

    // ====================== FORMULARIO (IZQUIERDA) ======================
    private JComponent crearPanelFormulario() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(16, 16, 16, 8));
        contenedor.setPreferredSize(new Dimension(310, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(crearBordeTitulado("Datos del país"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        txtId = crearCampo();
        txtId.setEditable(false);
        txtId.setBackground(new Color(0xEFEFEF));
        txtNombre = crearCampo();
        txtCapital = crearCampo();
        txtRegion = crearCampo();
        txtPoblacion = crearCampo();
        txtMoneda = crearCampo();

        int fila = 0;
        addCampo(form, g, fila++, "ID:", txtId);
        addCampo(form, g, fila++, "Nombre:", txtNombre);
        addCampo(form, g, fila++, "Capital:", txtCapital);
        addCampo(form, g, fila++, "Región:", txtRegion);
        addCampo(form, g, fila++, "Población:", txtPoblacion);
        addCampo(form, g, fila++, "Moneda:", txtMoneda);

        JPanel acciones = new JPanel(new GridLayout(4, 1, 0, 8));
        acciones.setOpaque(false);
        acciones.setBorder(new EmptyBorder(16, 0, 4, 0));
        btnGuardar = crearBoton("Guardar nuevo", COLOR_SUCCESS);
        btnActualizar = crearBoton("Modificar seleccionado", COLOR_PRIMARY);
        btnEliminar = crearBoton("Eliminar seleccionado", COLOR_DANGER);
        btnLimpiar = crearBoton("Limpiar campos", COLOR_NEUTRAL);
        acciones.add(btnGuardar);
        acciones.add(btnActualizar);
        acciones.add(btnEliminar);
        acciones.add(btnLimpiar);

        g.gridx = 0;
        g.gridy = fila;
        g.gridwidth = 2;
        g.weightx = 1;
        form.add(acciones, g);

        contenedor.add(form, BorderLayout.NORTH);
        contenedor.add(new ImagenAjustada("/Monster.png"), BorderLayout.CENTER);
        return contenedor;
    }

    /** Dibuja una imagen del classpath escalada para caber en el espacio disponible, sin deformarla. */
    private static class ImagenAjustada extends JComponent {
        private BufferedImage imagen;

        ImagenAjustada(String recurso) {
            try (java.io.InputStream in = FrmPais.class.getResourceAsStream(recurso)) {
                if (in != null) imagen = ImageIO.read(in);
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
            setBorder(new EmptyBorder(12, 0, 0, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (imagen == null) return;
            Insets in = getInsets();
            int disponibleW = getWidth() - in.left - in.right;
            int disponibleH = getHeight() - in.top - in.bottom;
            if (disponibleW <= 0 || disponibleH <= 0) return;

            double escala = Math.min((double) disponibleW / imagen.getWidth(),
                                     (double) disponibleH / imagen.getHeight());
            int w = (int) (imagen.getWidth() * escala);
            int h = (int) (imagen.getHeight() * escala);
            int x = in.left + (disponibleW - w) / 2;
            int y = in.top + (disponibleH - h) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(imagen, x, y, w, h, null);
            g2.dispose();
        }
    }

    private void addCampo(JPanel form, GridBagConstraints g, int fila, String etiqueta, JComponent campo) {
        g.gridy = fila;
        g.gridwidth = 1;
        g.gridx = 0;
        g.weightx = 0;
        JLabel l = new JLabel(etiqueta);
        l.setFont(FUENTE_BOLD);
        l.setForeground(COLOR_HEADER);
        form.add(l, g);

        g.gridx = 1;
        g.weightx = 1;
        form.add(campo, g);
    }

    // ====================== CENTRO (TOOLBAR + TABLA) ======================
    private JComponent crearPanelCentral() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 10));
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(16, 8, 8, 16));

        // ---- Barra de herramientas ----
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izq.setOpaque(false);
        btnImportarTodo = crearBoton("Importar todo (API)", COLOR_SUCCESS);
        btnImportarTodo.setToolTipText("Descarga todos los países desde la API y los guarda en MySQL");
        btnVaciar = crearBoton("Vaciar base de datos", COLOR_DANGER);
        btnVaciar.setToolTipText("Elimina TODOS los países de MySQL (pide confirmación)");
        izq.add(btnImportarTodo);
        izq.add(btnVaciar);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        der.setOpaque(false);
        JLabel lblFiltro = new JLabel("Filtrar región:");
        lblFiltro.setFont(FUENTE_BOLD);
        lblFiltro.setForeground(COLOR_HEADER);
        comboRegiones = new JComboBox<>(new String[]{"Ver Todos", "Americas", "Europe", "Asia", "Africa", "Oceania"});
        comboRegiones.setFont(FUENTE_NORMAL);
        btnReporte = crearBoton("Ver reportes", COLOR_HEADER);
        btnReporte.setToolTipText("Muestra estadísticas calculadas desde la base de datos");
        der.add(lblFiltro);
        der.add(comboRegiones);
        der.add(btnReporte);

        toolbar.add(izq, BorderLayout.WEST);
        toolbar.add(der, BorderLayout.EAST);

        // ---- Tabla ----
        tblPaises = new JTable();
        configurarTabla();
        JScrollPane scroll = new JScrollPane(tblPaises);
        scroll.setBorder(crearBordeTitulado("Países registrados"));
        scroll.getViewport().setBackground(Color.WHITE);

        // ---- Buscador por nombre (solo interfaz) ----
        JPanel buscador = new JPanel(new BorderLayout(8, 0));
        buscador.setBackground(Color.WHITE);
        buscador.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel lblBuscar = new JLabel("Buscar país:");
        lblBuscar.setFont(FUENTE_BOLD);
        lblBuscar.setForeground(COLOR_HEADER);

        txtBuscar = crearCampo();
        txtBuscar.setToolTipText("Escriba el nombre del país a buscar");
        btnBuscar = crearBoton("Buscar", COLOR_PRIMARY);

        buscador.add(lblBuscar, BorderLayout.WEST);
        buscador.add(txtBuscar, BorderLayout.CENTER);
        buscador.add(btnBuscar, BorderLayout.EAST);

        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.setOpaque(false);
        norte.add(buscador, BorderLayout.NORTH);
        norte.add(toolbar, BorderLayout.CENTER);

        contenedor.add(norte, BorderLayout.NORTH);
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private void configurarTabla() {
        tblPaises.setRowHeight(26);
        tblPaises.setFont(FUENTE_NORMAL);
        tblPaises.setSelectionBackground(new Color(0xD6EAF8));
        tblPaises.setSelectionForeground(Color.BLACK);
        tblPaises.setGridColor(new Color(0xE8E8E8));
        tblPaises.setShowVerticalLines(false);
        tblPaises.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPaises.setAutoCreateRowSorter(true);
        tblPaises.getTableHeader().setFont(FUENTE_BOLD);
        tblPaises.getTableHeader().setReorderingAllowed(false);

        // Render: filas alternadas + población con separador de miles, alineada a la derecha.
        DefaultTableCellRenderer render = new DefaultTableCellRenderer() {
            private final NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("es", "EC"));

            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int columna) {
                Object mostrado = valor;
                int colModelo = t.convertColumnIndexToModel(columna);
                if (colModelo == 4 && valor instanceof Number) {
                    mostrado = nf.format(((Number) valor).longValue());
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                Component c = super.getTableCellRendererComponent(t, mostrado, sel, foco, fila, columna);
                if (!sel) {
                    c.setBackground(fila % 2 == 0 ? Color.WHITE : COLOR_FILA_ALT);
                }
                return c;
            }
        };
        tblPaises.setDefaultRenderer(Object.class, render);
    }

    // ====================== BARRA DE ESTADO ======================
    private JComponent crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(0xDDE1E3));
        barra.setBorder(new EmptyBorder(6, 16, 6, 16));
        lblEstado = new JLabel("Listo.");
        lblEstado.setFont(FUENTE_NORMAL);
        lblEstado.setForeground(COLOR_HEADER);
        barra.add(lblEstado, BorderLayout.WEST);
        return barra;
    }

    // ====================== EVENTOS ======================
    private void registrarEventos() {
        tblPaises.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int fila = tblPaises.getSelectedRow();
            if (fila >= 0) {
                txtId.setText(valorCelda(fila, 0));
                txtNombre.setText(valorCelda(fila, 1));
                txtCapital.setText(valorCelda(fila, 2));
                txtRegion.setText(valorCelda(fila, 3));
                txtPoblacion.setText(valorCelda(fila, 4));
                txtMoneda.setText(valorCelda(fila, 5));
            }
        });

        btnImportarTodo.addActionListener(e -> importarTodo());
        btnBuscar.addActionListener(e -> buscarPais());
        txtBuscar.addActionListener(e -> buscarPais());
        btnVaciar.addActionListener(e -> vaciarBaseDeDatos());
        btnGuardar.addActionListener(e -> guardarNuevo());
        btnActualizar.addActionListener(e -> modificar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> { limpiarCampos(); tblPaises.clearSelection(); });

        comboRegiones.addActionListener(e -> {
            String seleccion = String.valueOf(comboRegiones.getSelectedItem());
            if ("Ver Todos".equals(seleccion)) {
                vistaActual = () -> controller.listarPaises();
            } else {
                vistaActual = () -> controller.filtrarPaisesPorRegion(seleccion);
            }
            recargarTabla();
        });

        btnReporte.addActionListener(e -> mostrarReportes());
    }

    private String valorCelda(int fila, int columna) {
        Object v = tblPaises.getValueAt(fila, columna);
        return v == null ? "" : v.toString();
    }

    // ====================== ACCIONES DE RED (en segundo plano) ======================
    private void importarTodo() {
        setUiTrabajando(true, "Importando países desde la API, espere...");
        new SwingWorker<PaisController.ResultadoImportacion, Void>() {
            @Override
            protected PaisController.ResultadoImportacion doInBackground() {
                return controller.importarPaises();
            }

            @Override
            protected void done() {
                PaisController.ResultadoImportacion r = null;
                try { r = get(); } catch (Exception ex) { ex.printStackTrace(); }
                setUiTrabajando(false, "Listo.");
                mostrarTodos();
                mostrarResultadoImportacion(r);
            }
        }.execute();
    }

    /** Busca en MySQL y, si no existe, en la API (guardando el resultado). */
    private void buscarPais() {
        String buscar = txtBuscar.getText().trim();
        if (buscar.isEmpty()) {
            mostrarTodos();
            return;
        }
        setUiTrabajando(true, "Buscando \"" + buscar + "\"...");
        new SwingWorker<PaisController.ResultadoBusqueda, Void>() {
            @Override
            protected PaisController.ResultadoBusqueda doInBackground() {
                return controller.buscarPais(buscar);
            }

            @Override
            protected void done() {
                PaisController.ResultadoBusqueda resultado = null;
                try { resultado = get(); } catch (Exception ex) { ex.printStackTrace(); }
                setUiTrabajando(false, "Listo.");
                if (resultado == null || resultado.paises().isEmpty()) {
                    recargarTabla();
                    lblEstado.setText("No se encontró \"" + buscar + "\" en la base de datos ni en la API.");
                    return;
                }
                vistaActual = () -> controller.buscarEnBd(buscar);
                cargarTabla(resultado.paises());
                lblEstado.setText(resultado.paises().size() + " resultado(s) para \"" + buscar + "\" - "
                        + (resultado.desdeApi() ? "obtenido desde la API y guardado en MySQL"
                                                : "obtenido desde MySQL"));
                if (resultado.desdeApi()) {
                    JOptionPane.showMessageDialog(FrmPais.this,
                            "El país se cargó desde la API y se guardó en MySQL.",
                            "Cargado desde la API", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }.execute();
    }

    private void setUiTrabajando(boolean trabajando, String mensaje) {
        setCursor(Cursor.getPredefinedCursor(trabajando ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        btnImportarTodo.setEnabled(!trabajando);
        btnBuscar.setEnabled(!trabajando);
        if (mensaje != null) lblEstado.setText(mensaje);
    }

    // ====================== ACCIONES CRUD ======================
    private void modificar() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un país de la tabla primero.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Pais p = leerFormulario();
        if (p == null) return;
        p.setId(Integer.parseInt(txtId.getText()));

        if (controller.modificarPais(p)) {
            recargarTabla();
            JOptionPane.showMessageDialog(this, "Registro actualizado en MySQL.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            mostrarError("No se pudo actualizar el registro.\n"
                       + "Es posible que ya exista otro país con el nombre \"" + p.getNombre() + "\".");
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarResultadoImportacion(PaisController.ResultadoImportacion r) {
        if (r == null || r.recibidos() == 0) {
            JOptionPane.showMessageDialog(this,
                    "La API no devolvió ningún país.\n\n"
                  + "Verifica tu API key en ConsumoAPI.java y tu conexión a internet.\n"
                  + "Revisa la consola para ver el detalle del error.",
                    "Sin datos de la API", JOptionPane.WARNING_MESSAGE);
        } else if (r.guardados() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Importación finalizada.\n\n"
                  + "Países nuevos guardados: " + r.guardados() + "\n"
                  + "Países que ya existían: " + r.existentes(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else if (r.existentes() == r.recibidos()) {
            JOptionPane.showMessageDialog(this,
                    "La API respondió correctamente con " + r.recibidos() + " países,\n"
                  + "ya registrados\n\n"
                  + "No se agregó ninguno nuevo.",
                    "Datos ya importados", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "La API respondió con " + r.recibidos() + " países, pero no se pudieron guardar.\n\n"
                  + "Verifica que MySQL esté activo.\n"
                  + "Revisa la consola para ver el detalle del error.",
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void vaciarBaseDeDatos() {
        String respuesta = JOptionPane.showInputDialog(this,
                "Esto eliminará TODOS los países de la base de datos y no se puede deshacer.\n"
              + "Para confirmar, escribe: eliminar",
                "Vaciar base de datos", JOptionPane.WARNING_MESSAGE);
        if (respuesta == null) return; // canceló
        if (!"eliminar".equals(respuesta.trim())) {
            JOptionPane.showMessageDialog(this, "Texto incorrecto. No se eliminó nada.",
                    "Cancelado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (controller.vaciarPaises()) {
            limpiarCampos();
            mostrarTodos();
            JOptionPane.showMessageDialog(this, "Base de datos vaciada.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            mostrarError("No se pudo vaciar la base de datos. Revisa la consola.");
        }
    }

    private void guardarNuevo() {
        Pais p = leerFormulario();
        if (p == null) return;

        if (controller.existePais(p.getNombre())) {
            JOptionPane.showMessageDialog(this, "Ya existe un país llamado \"" + p.getNombre() + "\".",
                    "Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (controller.crearPais(p)) {
            limpiarCampos();
            mostrarTodos();
            JOptionPane.showMessageDialog(this, "País guardado en MySQL.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            mostrarError("No se pudo guardar el país. Revisa la consola.");
        }
    }

    /** Lee y valida el formulario. Devuelve null (y avisa al usuario) si hay datos inválidos. */
    private Pais leerFormulario() {
        String nombre = txtNombre.getText().trim();
        String capital = txtCapital.getText().trim();
        String region = txtRegion.getText().trim();
        String moneda = txtMoneda.getText().trim();
        String poblacionTxt = txtPoblacion.getText().replaceAll("[^0-9]", "");

        if (nombre.isEmpty() || capital.isEmpty() || region.isEmpty() || moneda.isEmpty()
                || poblacionTxt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        try {
            return new Pais(nombre, capital, region, Long.parseLong(poblacionTxt), moneda);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La población debe ser un número válido.",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void eliminar() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un país de la tabla primero.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el país \"" + txtNombre.getText() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(txtId.getText());
        if (controller.borrarPais(id)) {
            limpiarCampos();
            recargarTabla();
            JOptionPane.showMessageDialog(this, "Registro eliminado de MySQL.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            mostrarError("No se pudo eliminar el registro. Es posible que ya no exista.");
        }
    }

    private void mostrarReportes() {
        String masPoblado = controller.obtenerPaisMasPoblado();
        Map<String, Integer> conteo = controller.obtenerConteoPorRegion();

        StringBuilder porRegion = new StringBuilder();
        int total = 0;
        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            porRegion.append("   - ").append(nombreRegion(e.getKey()))
                     .append(": ").append(e.getValue()).append("\n");
            total += e.getValue();
        }

        // El mapa viene ordenado de mayor a menor: la primera entrada es la región con más países.
        String regionTop = conteo.isEmpty()
                ? "Sin datos"
                : nombreRegion(conteo.keySet().iterator().next())
                  + " (" + conteo.values().iterator().next() + " países)";

        String mensaje = "=== REPORTES ESTADÍSTICOS ===\n\n"
                + "• País más poblado: " + masPoblado + "\n\n"
                + "• Región con más países: " + regionTop + "\n\n"
                + "• Cantidad de países por región:\n"
                + porRegion
                + "   Total: " + total + "\n";
        JOptionPane.showMessageDialog(this, mensaje, "Estadísticas del Sistema",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Traduce el nombre de región de la API; si no se conoce, lo deja tal cual. */
    private String nombreRegion(String region) {
        return switch (region) {
            case "Americas" -> "Américas";
            case "Europe" -> "Europa";
            case "Africa" -> "África";
            case "Oceania" -> "Oceanía";
            case "Antarctic" -> "Antártida";
            default -> region;
        };
    }

    // ====================== TABLA / UTILIDADES ======================
    /** Vuelve a consultar la vista actual (filtro o búsqueda) para no perder el contexto. */
    private void recargarTabla() {
        cargarTabla(vistaActual.get());
    }

    /** Quita filtro y búsqueda y muestra todos los países. */
    private void mostrarTodos() {
        txtBuscar.setText("");
        comboRegiones.setSelectedIndex(0); // si cambia, su listener ya recarga
        vistaActual = () -> controller.listarPaises();
        recargarTabla();
    }

    private void cargarTabla(List<Pais> lista) {
        String[] columnas = {"ID", "Nombre", "Capital", "Región", "Población", "Moneda"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (Pais p : lista) {
            model.addRow(new Object[]{p.getId(), p.getNombre(), p.getCapital(),
                    p.getRegion(), p.getPoblacion(), p.getMoneda()});
        }
        tblPaises.setModel(model);

        // ID estrecho
        if (tblPaises.getColumnModel().getColumnCount() == 6) {
            tblPaises.getColumnModel().getColumn(0).setMaxWidth(60);
            tblPaises.getColumnModel().getColumn(0).setPreferredWidth(45);
        }
        lblEstado.setText("Total de países mostrados: " + lista.size());
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtCapital.setText("");
        txtRegion.setText("");
        txtPoblacion.setText("");
        txtMoneda.setText("");
    }

    // ====================== HELPERS DE ESTILO ======================
    private JTextField crearCampo() {
        JTextField t = new JTextField();
        t.setFont(FUENTE_NORMAL);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                new EmptyBorder(5, 8, 5, 8)));
        return t;
    }

    private Border crearBordeTitulado(String titulo) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDE), titulo);
        tb.setTitleFont(FUENTE_BOLD);
        tb.setTitleColor(COLOR_HEADER);
        return BorderFactory.createCompoundBorder(tb, new EmptyBorder(8, 8, 8, 8));
    }

    /** Botón plano de color, pintado a mano para verse igual en cualquier Look & Feel. */
    private JButton crearBoton(String texto, Color base) {
        JButton b = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondo = base;
                if (!isEnabled()) {
                    fondo = COLOR_NEUTRAL;
                } else if (getModel().isPressed()) {
                    fondo = base.darker();
                } else if (getModel().isRollover()) {
                    fondo = base.brighter();
                }
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FUENTE_BOLD);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 16, 9, 16));
        return b;
    }
}
