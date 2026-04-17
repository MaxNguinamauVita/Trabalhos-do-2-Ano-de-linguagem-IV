import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

// ==================== ENUMS ====================

enum StatusEvento {
    PENDENTE("Pendente"),
    CONFIRMADO("Confirmado"),
    CANCELADO("Cancelado"),
    CONCLUIDO("Concluído");

    private final String descricao;

    StatusEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

enum CategoriaEvento {
    CONFERENCIA("Conferência"),
    WORKSHOP("Workshop"),
    SEMINARIO("Seminário"),
    FESTA("Festa"),
    ESPORTIVO("Esportivo"),
    CULTURAL("Cultural");

    private final String descricao;

    CategoriaEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

enum StatusInscricao {
    ATIVA("Ativa"),
    CANCELADA("Cancelada"),
    CONFIRMADA("Confirmada");

    private final String descricao;

    StatusInscricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

// ==================== EXCEÇÃO PERSONALIZADA ====================

class EventoException extends Exception {
    private static final long serialVersionUID = 1L;

    public EventoException(String mensagem) {
        super(mensagem);
    }

    public EventoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

// ==================== CLASSES IMUTÁVEIS ====================

final class Evento {
    private final Long id;
    private final String nome;
    private final String descricao;
    private final LocalDateTime dataHora;
    private final String local;
    private final CategoriaEvento categoria;
    private final Integer capacidadeMaxima;
    private StatusEvento status;

    public Evento(Long id, String nome, String descricao, LocalDateTime dataHora,
                  String local, CategoriaEvento categoria, Integer capacidadeMaxima) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.dataHora = dataHora;
        this.local = local;
        this.categoria = categoria;
        this.capacidadeMaxima = capacidadeMaxima;
        this.status = StatusEvento.PENDENTE;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getLocal() { return local; }
    public CategoriaEvento getCategoria() { return categoria; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public StatusEvento getStatus() { return status; }

    public void setStatus(StatusEvento status) {
        if (status != null) {
            this.status = status;
        }
    }

    public boolean isDisponivel() {
        return status == StatusEvento.PENDENTE || status == StatusEvento.CONFIRMADO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evento evento = (Evento) o;
        return Objects.equals(id, evento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Evento{id=%d, nome='%s', data=%s, local='%s', categoria=%s, status=%s}",
                id, nome, dataHora, local, categoria.getDescricao(), status.getDescricao());
    }
}

final class Participante {
    private final Long id;
    private final String nome;
    private final String email;
    private final String telefone;

    public Participante(Long id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participante that = (Participante) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Participante{id=%d, nome='%s', email='%s'}", id, nome, email);
    }
}

final class Inscricao {
    private final Long id;
    private final Participante participante;
    private final Evento evento;
    private final LocalDateTime dataInscricao;
    private StatusInscricao status;

    public Inscricao(Long id, Participante participante, Evento evento) {
        this.id = id;
        this.participante = participante;
        this.evento = evento;
        this.dataInscricao = LocalDateTime.now();
        this.status = StatusInscricao.ATIVA;
    }

    public Long getId() { return id; }
    public Participante getParticipante() { return participante; }
    public Evento getEvento() { return evento; }
    public LocalDateTime getDataInscricao() { return dataInscricao; }
    public StatusInscricao getStatus() { return status; }

    public void cancelar() {
        if (status == StatusInscricao.ATIVA) {
            this.status = StatusInscricao.CANCELADA;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inscricao inscricao = (Inscricao) o;
        return Objects.equals(id, inscricao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Inscricao{id=%d, participante=%s, evento=%s, status=%s}",
                id, participante.getNome(), evento.getNome(), status.getDescricao());
    }
}

// ==================== SERVIÇOS ====================

class EventoService {
    private final Map<Long, Evento> eventos = new HashMap<>();
    private Long proximoId = 1L;

    public Evento criarEvento(String nome, String descricao, LocalDateTime dataHora,
                              String local, CategoriaEvento categoria, Integer capacidadeMaxima)
            throws EventoException {

        if (nome == null || nome.trim().isEmpty()) {
            throw new EventoException("Nome do evento é obrigatório");
        }

        if (descricao == null || descricao.trim().isEmpty()) {
            throw new EventoException("Descrição do evento é obrigatória");
        }

        if (dataHora == null) {
            throw new EventoException("Data e hora do evento são obrigatórias");
        }

        if (local == null || local.trim().isEmpty()) {
            throw new EventoException("Local do evento é obrigatório");
        }

        if (categoria == null) {
            throw new EventoException("Categoria do evento é obrigatória");
        }

        if (capacidadeMaxima == null || capacidadeMaxima <= 0) {
            throw new EventoException("Capacidade máxima deve ser maior que zero");
        }

        Evento evento = new Evento(proximoId++, nome, descricao, dataHora,
                local, categoria, capacidadeMaxima);
        eventos.put(evento.getId(), evento);
        return evento;
    }

    public Evento buscarEventoPorId(Long id) throws EventoException {
        if (id == null) {
            throw new EventoException("ID do evento não pode ser nulo");
        }

        Evento evento = eventos.get(id);
        if (evento == null) {
            throw new EventoException("Evento não encontrado com ID: " + id);
        }
        return evento;
    }

    public List<Evento> listarTodosEventos() {
        return new ArrayList<>(eventos.values());
    }

    public List<Evento> listarEventosPorCategoria(CategoriaEvento categoria) {
        if (categoria == null) {
            return new ArrayList<>();
        }

        List<Evento> resultado = new ArrayList<>();
        for (Evento evento : eventos.values()) {
            if (evento.getCategoria() == categoria) {
                resultado.add(evento);
            }
        }
        return resultado;
    }

    public List<Evento> listarEventosFuturos() {
        List<Evento> resultado = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        for (Evento evento : eventos.values()) {
            if (evento.getDataHora().isAfter(agora)) {
                resultado.add(evento);
            }
        }
        return resultado;
    }

    public void atualizarStatusEvento(Long id, StatusEvento status) throws EventoException {
        if (status == null) {
            throw new EventoException("Estado de Evento não pode ser nulo");
        }

        Evento evento = buscarEventoPorId(id);
        evento.setStatus(status);
    }

    public void cancelarEvento(Long id) throws EventoException {
        Evento evento = buscarEventoPorId(id);
        evento.setStatus(StatusEvento.CANCELADO);
    }

    public int getTotalEventos() {
        return eventos.size();
    }
}

class ParticipanteService {
    private final Map<Long, Participante> participantes = new HashMap<>();
    private Long proximoId = 1L;

    public Participante cadastrarParticipante(String nome, String email, String telefone)
            throws EventoException {

        if (nome == null || nome.trim().isEmpty()) {
            throw new EventoException("Nome do participante é obrigatório");
        }

        if (email == null || !email.contains("@") || email.trim().isEmpty()) {
            throw new EventoException("Email inválido");
        }

        for (Participante p : participantes.values()) {
            if (p.getEmail().equalsIgnoreCase(email.trim())) {
                throw new EventoException("Email já cadastrado: " + email);
            }
        }

        Participante participante = new Participante(proximoId++, nome.trim(), email.trim(), telefone);
        participantes.put(participante.getId(), participante);
        return participante;
    }

    public Participante buscarParticipantePorId(Long id) throws EventoException {
        if (id == null) {
            throw new EventoException("ID do participante não pode ser nulo");
        }

        Participante participante = participantes.get(id);
        if (participante == null) {
            throw new EventoException("Participante não encontrado com ID: " + id);
        }
        return participante;
    }

    public List<Participante> listarTodosParticipantes() {
        return new ArrayList<>(participantes.values());
    }

    public int getTotalParticipantes() {
        return participantes.size();
    }
}

class InscricaoService {
    private final Map<Long, Inscricao> inscricoes = new HashMap<>();
    private final Map<Long, List<Inscricao>> inscricoesPorEvento = new HashMap<>();
    private final Map<Long, List<Inscricao>> inscricoesPorParticipante = new HashMap<>();
    private Long proximoId = 1L;

    private final EventoService eventoService;
    private final ParticipanteService participanteService;

    public InscricaoService(EventoService eventoService, ParticipanteService participanteService) {
        this.eventoService = eventoService;
        this.participanteService = participanteService;
    }

    public Inscricao realizarInscricao(Long eventoId, Long participanteId) throws EventoException {
        if (eventoId == null || participanteId == null) {
            throw new EventoException("ID do evento e participante não podem ser nulos");
        }

        Evento evento = eventoService.buscarEventoPorId(eventoId);
        Participante participante = participanteService.buscarParticipantePorId(participanteId);

        if (!evento.isDisponivel()) {
            throw new EventoException("Evento não está disponível para inscrições. Status: "
                    + evento.getStatus().getDescricao());
        }

        List<Inscricao> inscricoesParticipante = inscricoesPorParticipante.get(participanteId);
        if (inscricoesParticipante != null) {
            for (Inscricao i : inscricoesParticipante) {
                if (i.getEvento().getId().equals(eventoId) && i.getStatus() == StatusInscricao.ATIVA) {
                    throw new EventoException("Participante já está inscrito ativamente neste evento");
                }
            }
        }

        long vagasOcupadas = contarInscricoesAtivasPorEvento(eventoId);
        if (vagasOcupadas >= evento.getCapacidadeMaxima()) {
            throw new EventoException("Evento lotado. Capacidade máxima: "
                    + evento.getCapacidadeMaxima());
        }

        Inscricao inscricao = new Inscricao(proximoId++, participante, evento);
        inscricoes.put(inscricao.getId(), inscricao);

        inscricoesPorEvento.computeIfAbsent(eventoId, k -> new ArrayList<>()).add(inscricao);
        inscricoesPorParticipante.computeIfAbsent(participanteId, k -> new ArrayList<>()).add(inscricao);

        return inscricao;
    }

    public void cancelarInscricao(Long inscricaoId) throws EventoException {
        if (inscricaoId == null) {
            throw new EventoException("ID da inscrição não pode ser nulo");
        }

        Inscricao inscricao = inscricoes.get(inscricaoId);
        if (inscricao == null) {
            throw new EventoException("Inscrição não encontrada com ID: " + inscricaoId);
        }

        Evento evento = inscricao.getEvento();
        if (evento.getStatus() == StatusEvento.CONCLUIDO) {
            throw new EventoException("Não é possível cancelar inscrição de evento já concluído");
        }

        if (evento.getStatus() == StatusEvento.CANCELADO) {
            throw new EventoException("Não é possível cancelar inscrição de evento cancelado");
        }

        inscricao.cancelar();
    }

    public List<Inscricao> listarInscricoesPorEvento(Long eventoId) throws EventoException {
        if (eventoId == null) {
            throw new EventoException("ID do evento não pode ser nulo");
        }

        eventoService.buscarEventoPorId(eventoId);
        return new ArrayList<>(inscricoesPorEvento.getOrDefault(eventoId, new ArrayList<>()));
    }

    public List<Inscricao> listarInscricoesPorParticipante(Long participanteId) throws EventoException {
        if (participanteId == null) {
            throw new EventoException("ID do participante não pode ser nulo");
        }

        participanteService.buscarParticipantePorId(participanteId);
        return new ArrayList<>(inscricoesPorParticipante.getOrDefault(participanteId, new ArrayList<>()));
    }

    public long contarInscricoesAtivasPorEvento(Long eventoId) {
        if (eventoId == null) {
            return 0;
        }

        List<Inscricao> inscricoes = inscricoesPorEvento.get(eventoId);
        if (inscricoes == null) {
            return 0;
        }

        long count = 0;
        for (Inscricao i : inscricoes) {
            if (i.getStatus() == StatusInscricao.ATIVA) {
                count++;
            }
        }
        return count;
    }

    public int getTotalInscricoes() {
        return inscricoes.size();
    }
}

// ==================== CLASSE PRINCIPAL INTERATIVA ====================

public class SistemaGestaoEventos {
    private static EventoService eventoService = new EventoService();
    private static ParticipanteService participanteService = new ParticipanteService();
    private static InscricaoService inscricaoService = new InscricaoService(eventoService, participanteService);
    private static Scanner scanner = new Scanner(System.in);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        boolean continuar = true;

        while (continuar) {
            exibirMenuPrincipal();
            int opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    menuEventos();
                    break;
                case 2:
                    menuParticipantes();
                    break;
                case 3:
                    menuInscricoes();
                    break;
                case 4:
                    menuConsultas();
                    break;
                case 5:
                    menuEstatisticas();
                    break;
                case 0:
                    continuar = false;
                    System.out.println("\n=== SISTEMA ENCERRADO. OBRIGADO! ===\n");
                    break;
                default:
                    System.out.println("❌ Opção inválida! Tente novamente.\n");
            }
        }
        scanner.close();
    }

    private static void exibirMenuPrincipal() {
        System.out.println("\n" + repetir("=", 50));
        System.out.println("       SISTEMA DE GESTÃO DE EVENTOS");
        System.out.println(repetir("=", 50));
        System.out.println("1. 📅 Eventos");
        System.out.println("2. 👥 Participantes");
        System.out.println("3. 📝 Inscrições");
        System.out.println("4. 🔍 Consultas");
        System.out.println("5. 📊 Estatísticas");
        System.out.println("0. 🚪 Sair");
        System.out.println(repetir("-", 50));
    }

    // Método auxiliar para repetir strings (compatível com Java 8)
    private static String repetir(String str, int vezes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vezes; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    // ==================== MENU EVENTOS ====================

    private static void menuEventos() {
        while (true) {
            System.out.println("\n--- GERENCIAMENTO DE EVENTOS ---");
            System.out.println("1. Criar novo evento");
            System.out.println("2. Consultar todos eventos");
            System.out.println("3. Consultar evento por ID");
            System.out.println("4. Atualizar eventos");
            System.out.println("5. Cancelar evento");
            System.out.println("6. Pesquisar eventos por categoria");
            System.out.println("7. Pesquisar eventos futuros");
            System.out.println("0. Voltar");

            int opcao = lerInteiro("Faça a sua Escolha: ");

            switch (opcao) {
                case 1:
                    criarEvento();
                    break;
                case 2:
                    listarTodosEventos();
                    break;
                case 3:
                    buscarEventoPorId();
                    break;
                case 4:
                    atualizarStatusEvento();
                    break;
                case 5:
                    cancelarEvento();
                    break;
                case 6:
                    listarEventosPorCategoria();
                    break;
                case 7:
                    listarEventosFuturos();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("❌ Opção inválida!");
            }
        }
    }

    private static void criarEvento() {
        System.out.println("\n--- CRIAR EVENTO ---");

        try {
            System.out.print("Nome do evento: ");
            String nome = scanner.nextLine();

            System.out.print("Descrição: ");
            String descricao = scanner.nextLine();

            System.out.print("Data e hora (formato: dd/MM/yyyy HH:mm): ");
            String dataHoraStr = scanner.nextLine();
            LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, formatter);

            System.out.print("Local: ");
            String local = scanner.nextLine();

            System.out.println("\nCategorias disponíveis:");
            CategoriaEvento[] categorias = CategoriaEvento.values();
            for (int i = 0; i < categorias.length; i++) {
                System.out.println((i+1) + ". " + categorias[i].getDescricao());
            }
            int catOpcao = lerInteiro("Escolha a categoria (1-" + categorias.length + "): ");
            CategoriaEvento categoria = categorias[catOpcao - 1];

            int capacidade = lerInteiro("Capacidade máxima: ");

            Evento evento = eventoService.criarEvento(nome, descricao, dataHora, local, categoria, capacidade);
            System.out.println("✅ Evento adicionado com sucesso! ID: " + evento.getId());

        } catch (DateTimeParseException e) {
            System.out.println("❌ Data/hora inválida! Use o formato: dd/MM/yyyy HH:mm");
        } catch (EventoException e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private static void listarTodosEventos() {
        System.out.println("\n--- TODOS OS EVENTOS ---");
        List<Evento> eventos = eventoService.listarTodosEventos();
        if (eventos.isEmpty()) {
            System.out.println("Nenhum evento cadastrado.");
        } else {
            eventos.forEach(System.out::println);
        }
    }

    private static void buscarEventoPorId() {
        System.out.println("\n--- BUSCAR EVENTO ---");
        Long id = (long) lerInteiro("ID do evento: ");
        try {
            Evento evento = eventoService.buscarEventoPorId(id);
            System.out.println("📅 Evento encontrado:");
            System.out.println("   ID: " + evento.getId());
            System.out.println("   Nome: " + evento.getNome());
            System.out.println("   Descrição: " + evento.getDescricao());
            System.out.println("   Data: " + evento.getDataHora().format(formatter));
            System.out.println("   Local: " + evento.getLocal());
            System.out.println("   Categoria: " + evento.getCategoria().getDescricao());
            System.out.println("   Capacidade Máxima: " + evento.getCapacidadeMaxima());
            System.out.println("   Estado de Evento: " + evento.getStatus().getDescricao());
        } catch (EventoException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void atualizarStatusEvento() {
        System.out.println("\n--- ATUALIZAR STATUS DO EVENTO ---");
        Long id = (long) lerInteiro("ID do evento: ");

        try {
            Evento evento = eventoService.buscarEventoPorId(id);
            System.out.println("Estado de Evento atual: " + evento.getStatus().getDescricao());

            System.out.println("\nNovo Estado:");
            StatusEvento[] statuses = StatusEvento.values();
            for (int i = 0; i < statuses.length; i++) {
                System.out.println((i+1) + ". " + statuses[i].getDescricao());
            }
            int statusOpcao = lerInteiro("Escolha (1-" + statuses.length + "): ");

            eventoService.atualizarStatusEvento(id, statuses[statusOpcao - 1]);
            System.out.println("✅ Estado de Evento atualizado com sucesso!");
        } catch (EventoException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void cancelarEvento() {
        System.out.println("\n--- CANCELAR EVENTO ---");
        Long id = (long) lerInteiro("ID do evento para cancelar: ");

        try {
            eventoService.cancelarEvento(id);
            System.out.println("✅ Evento cancelado com sucesso!");
        } catch (EventoException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void listarEventosPorCategoria() {
        System.out.println("\n--- EVENTOS POR CATEGORIA ---");
        CategoriaEvento[] categorias = CategoriaEvento.values();
        for (int i = 0; i < categorias.length; i++) {
            System.out.println((i+1) + ". " + categorias[i].getDescricao());
        }
        int catOpcao = lerInteiro("Faça a sua Escolha de categoria: ");
        CategoriaEvento categoria = categorias[catOpcao - 1];

        List<Evento> eventos = eventoService.listarEventosPorCategoria(categoria);
        if (eventos.isEmpty()) {
            System.out.println("Nenhum evento encontrado nesta categoria.");
        } else {
            eventos.forEach(System.out::println);
        }
    }

    private static void listarEventosFuturos() {
        System.out.println("\n--- EVENTOS FUTUROS ---");
        List<Evento> eventos = eventoService.listarEventosFuturos();
        if (eventos.isEmpty()) {
            System.out.println("Nenhum evento futuro encontrado.");
        } else {
            eventos.forEach(System.out::println);
        }
    }

    // ==================== MENU PARTICIPANTES ====================

    private static void menuParticipantes() {
        while (true) {
            System.out.println("\n--- GERENCIAMENTO DE PARTICIPANTES ---");
            System.out.println("1. Cadastrar participante");
            System.out.println("2. Consultar todos participantes");
            System.out.println("3. Consultar participante por ID");
            System.out.println("0. Voltar");

            int opcao = lerInteiro("Faça a sua Escolha: ");

            switch (opcao) {
                case 1:
                    cadastrarParticipante();
                    break;
                case 2:
                    listarTodosParticipantes();
                    break;
                case 3:
                    buscarParticipantePorId();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("❌ Opção inválida!");
            }
        }
    }

    private static void cadastrarParticipante() {
        System.out.println("\n--- CADASTRAR PARTICIPANTE ---");

        try {
            System.out.print("Nome completo: ");
            String nome = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Telefone: ");
            String telefone = scanner.nextLine();

            Participante participante = participanteService.cadastrarParticipante(nome, email, telefone);
            System.out.println("✅ Participante cadastrado com sucesso! ID: " + participante.getId());

        } catch (EventoException e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private static void listarTodosParticipantes() {
        System.out.println("\n--- TODOS OS PARTICIPANTES ---");
        List<Participante> participantes = participanteService.listarTodosParticipantes();
        if (participantes.isEmpty()) {
            System.out.println("Nenhum participante cadastrado.");
        } else {
            participantes.forEach(System.out::println);
        }
    }

    private static void buscarParticipantePorId() {
        System.out.println("\n--- CONSULTAR PARTICIPANTES ---");
        Long id = (long) lerInteiro("ID do participante: ");
        try {
            Participante participante = participanteService.buscarParticipantePorId(id);
            System.out.println("👤 Participante encontrado:");
            System.out.println("   ID: " + participante.getId());
            System.out.println("   Nome: " + participante.getNome());
            System.out.println("   Email: " + participante.getEmail());
            System.out.println("   Telefone: " + participante.getTelefone());
        } catch (EventoException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ==================== MENU INSCRIÇÕES ====================

    private static void menuInscricoes() {
        while (true) {
            System.out.println("\n--- GERENCIAMENTO DE INSCRIÇÕES ---");
            System.out.println("1. Realizar inscrição");
            System.out.println("2. Cancelar inscrição");
            System.out.println("3. Consultar inscrições por evento");
            System.out.println("4. Consultar inscrições por participante");
            System.out.println("0. Voltar");

            int opcao = lerInteiro("Faça a sua Escolha: ");

            switch (opcao) {
                case 1:
                    realizarInscricao();
                    break;
                case 2:
                    cancelarInscricao();
                    break;
                case 3:
                    listarInscricoesPorEvento();
                    break;
                case 4:
                    listarInscricoesPorParticipante();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("❌ Opção inválida!");
            }
        }
    }

    private static void realizarInscricao() {
        System.out.println("\n--- REALIZAR INSCRIÇÃO ---");

        try {
            System.out.println("\nEventos disponíveis:");
            List<Evento> eventos = eventoService.listarTodosEventos();
            if (eventos.isEmpty()) {
                System.out.println("Nenhum evento disponível.");
                return;
            }
            for (Evento e : eventos) {
                System.out.println("   ID: " + e.getId() + " - " + e.getNome() + " (" + e.getStatus().getDescricao() + ")");
            }
            Long eventoId = (long) lerInteiro("ID do evento: ");

            System.out.println("\nParticipantes cadastrados:");
            List<Participante> participantes = participanteService.listarTodosParticipantes();
            if (participantes.isEmpty()) {
                System.out.println("Nenhum participante cadastrado.");
                return;
            }
            for (Participante p : participantes) {
                System.out.println("   ID: " + p.getId() + " - " + p.getNome());
            }
            Long participanteId = (long) lerInteiro("ID do participante: ");

            Inscricao inscricao = inscricaoService.realizarInscricao(eventoId, participanteId);
            System.out.println("✅ Inscrição realizada com sucesso! ID: " + inscricao.getId());

        } catch (EventoException e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private static void cancelarInscricao() {
        System.out.println("\n--- CANCELAR INSCRIÇÃO ---");
        int inscricaoId = lerInteiro("ID da inscrição: ");

        try {
            inscricaoService.cancelarInscricao((long) inscricaoId);
            System.out.println("✅ Inscrição cancelada com sucesso!");
        } catch (EventoException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void listarInscricoesPorEvento() {
        System.out.println("\n--- INSCRIÇÕES POR EVENTO ---");
        Long eventoId = (long) lerInteiro("ID do evento: ");

        try {
            List<Inscricao> inscricoes = inscricaoService.listarInscricoesPorEvento(eventoId);
            if (inscricoes.isEmpty()) {
                System.out.println("Nenhuma inscrição encontrada para este evento.");
            } else {
                Evento evento = eventoService.buscarEventoPorId(eventoId);
                System.out.println("Evento: " + evento.getNome());
                System.out.println("Total de inscrições: " + inscricoes.size());
                System.out.println("Inscrições ativas: " + inscricaoService.contarInscricoesAtivasPorEvento(eventoId));
                System.out.println("\nLista de participantes:");
                for (Inscricao i : inscricoes) {
                    System.out.println("   - " + i.getParticipante().getNome() +
                            " (" + i.getStatus().getDescricao() + ")");
                }
            }
        } catch (EventoException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void listarInscricoesPorParticipante() {
        System.out.println("\n--- INSCRIÇÕES POR PARTICIPANTE ---");
        Long participanteId = (long) lerInteiro("ID do participante: ");

        try {
            List<Inscricao> inscricoes = inscricaoService.listarInscricoesPorParticipante(participanteId);
            if (inscricoes.isEmpty()) {
                System.out.println("Nenhuma inscrição encontrada para este participante.");
            } else {
                Participante participante = participanteService.buscarParticipantePorId(participanteId);
                System.out.println("Participante: " + participante.getNome());
                System.out.println("Total de inscrições: " + inscricoes.size());
                System.out.println("\nEventos inscritos:");
                for (Inscricao i : inscricoes) {
                    System.out.println("   - " + i.getEvento().getNome() +
                            " (" + i.getStatus().getDescricao() + ")");
                }
            }
        } catch (EventoException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ==================== MENU CONSULTAS ====================

    private static void menuConsultas() {
        while (true) {
            System.out.println("\n--- CONSULTAS ---");
            System.out.println("1. Verificar vagas disponíveis em evento");
            System.out.println("2. Consultar evento por nome (parcial)");
            System.out.println("3. Consultar eventos por status");
            System.out.println("0. Voltar");

            int opcao = lerInteiro("Faça uma Escolha: ");

            switch (opcao) {
                case 1:
                    verificarVagasDisponiveis();
                    break;
                case 2:
                    buscarEventoPorNome();
                    break;
                case 3:
                    listarEventosPorStatus();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("❌ Opção inválida!");
            }
        }
    }

    private static void verificarVagasDisponiveis() {
        System.out.println("\n=== VAGAS DISPONÍVEIS ====");
        Long eventoId = (long) lerInteiro("ID do evento: ");

        try {
            Evento evento = eventoService.buscarEventoPorId(eventoId);
            long vagasOcupadas = inscricaoService.contarInscricoesAtivasPorEvento(eventoId);
            long vagasDisponiveis = evento.getCapacidadeMaxima() - vagasOcupadas;

            System.out.println("Evento: " + evento.getNome());
            System.out.println("Capacidade total: " + evento.getCapacidadeMaxima());
            System.out.println("Vagas ocupadas: " + vagasOcupadas);
            System.out.println("Vagas disponíveis: " + vagasDisponiveis);
        } catch (EventoException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void buscarEventoPorNome() {
        System.out.println("\n==== PESQUISAR EVENTO POR NOME ====");
        System.out.print("Digite parte do nome: ");
        String nomeParcial = scanner.nextLine().toLowerCase();

        List<Evento> eventos = eventoService.listarTodosEventos();
        List<Evento> resultados = new ArrayList<>();

        for (Evento e : eventos) {
            if (e.getNome().toLowerCase().contains(nomeParcial)) {
                resultados.add(e);
            }
        }

        if (resultados.isEmpty()) {
            System.out.println("Nenhum evento encontrado com este nome.");
        } else {
            System.out.println("Eventos encontrados:");
            resultados.forEach(System.out::println);
        }
    }

    private static void listarEventosPorStatus() {
        System.out.println("\n==== EVENTOS POR ESTADO ====");
        StatusEvento[] statuses = StatusEvento.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i+1) + ". " + statuses[i].getDescricao());
        }
        int statusOpcao = lerInteiro("Escolha o Estado: ");
        StatusEvento status = statuses[statusOpcao - 1];

        List<Evento> todosEventos = eventoService.listarTodosEventos();
        List<Evento> resultados = new ArrayList<>();

        for (Evento e : todosEventos) {
            if (e.getStatus() == status) {
                resultados.add(e);
            }
        }

        if (resultados.isEmpty()) {
            System.out.println("Nenhum evento encontrado com este Estado.");
        } else {
            System.out.println("Eventos com Estado " + status.getDescricao() + ":");
            resultados.forEach(System.out::println);
        }
    }

    // ==================== MENU ESTATÍSTICAS ====================

    private static void menuEstatisticas() {
        System.out.println("\n--- ESTATÍSTICAS DO SISTEMA ---");
        System.out.println("📊 Total de eventos: " + eventoService.getTotalEventos());
        System.out.println("👥 Total de participantes: " + participanteService.getTotalParticipantes());
        System.out.println("📝 Total de inscrições: " + inscricaoService.getTotalInscricoes());

        System.out.println("\n📈 Eventos por categoria:");
        for (CategoriaEvento cat : CategoriaEvento.values()) {
            int quantidade = eventoService.listarEventosPorCategoria(cat).size();
            if (quantidade > 0) {
                System.out.println("   " + cat.getDescricao() + ": " + quantidade);
            }
        }

        System.out.println("\n📌 Eventos por Estado:");
        for (StatusEvento status : StatusEvento.values()) {
            int quantidade = 0;
            for (Evento e : eventoService.listarTodosEventos()) {
                if (e.getStatus() == status) quantidade++;
            }
            if (quantidade > 0) {
                System.out.println("   " + status.getDescricao() + ": " + quantidade);
            }
        }

        Evento eventoMaisInscricoes = null;
        long maxInscricoes = 0;
        for (Evento e : eventoService.listarTodosEventos()) {
            try {
                long inscricoes = inscricaoService.listarInscricoesPorEvento(e.getId()).size();
                if (inscricoes > maxInscricoes) {
                    maxInscricoes = inscricoes;
                    eventoMaisInscricoes = e;
                }
            } catch (EventoException ex) {}
        }

        if (eventoMaisInscricoes != null && maxInscricoes > 0) {
            System.out.println("\n🏆 Evento com mais inscrições: " + eventoMaisInscricoes.getNome() +
                    " (" + maxInscricoes + " inscrições)");
        }

        System.out.println("\nPressione ENTER para continuar...");
        scanner.nextLine();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private static int lerInteiro(String mensagem) {
        System.out.print(mensagem);
        while (!scanner.hasNextInt()) {
            System.out.print("❌ Valor inválido! " + mensagem);
            scanner.next();
        }
        int valor = scanner.nextInt();
        scanner.nextLine();
        return valor;
    }
}