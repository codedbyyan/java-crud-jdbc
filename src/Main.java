import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main.java
 * Projeto: CRUD Java + JDBC (com modo "sem DB" para testar sem MySQL)
 *
 * Como usar:
 * - Se você tem MySQL: configure URL, USER, PASS e deixe USE_DB = true.
 * - Se não tem MySQL: deixe USE_DB = false e rode a aplicação (usa memória).
 */

public class Main {

    // --- CONFIGURAÇÃO ---
    private static final boolean USE_DB = false; // coloque true quando for usar MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/meubanco"; // ajuste quando usar DB
    private static final String USER = "root"; // ajuste
    private static final String PASS = "senha"; // ajuste
    // ----------------------

    // usado no modo sem DB
    private static final List<User> memoryUsers = new ArrayList<>();
    private static int nextId = 1;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcao;

        if (USE_DB) {
            System.out.println("Modo: USANDO BANCO (JDBC). Lembre-se de configurar URL/USER/PASS.");
        } else {
            System.out.println("Modo: SEM BANCO (memória). Perfeito para testar sem instalar nada.");
        }

        do {
            System.out.println("\n=== CRUD Java (modo " + (USE_DB ? "DB" : "SEM DB") + ") ===");
            System.out.println("1 - Cadastrar usuário");
            System.out.println("2 - Listar usuários");
            System.out.println("3 - Atualizar usuário");
            System.out.println("4 - Deletar usuário");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");

            while (!sc.hasNextInt()) {
                System.out.println("Digite um número!");
                sc.next();
            }
            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1 -> cadastrar(sc);
                case 2 -> listar();
                case 3 -> atualizar(sc);
                case 4 -> deletar(sc);
                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida!");
            }

        } while (opcao != 0);
    }

    // conecta ao banco (modo DB)
    private static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void cadastrar(Scanner sc) {
        System.out.print("Nome: ");
        String nome = sc.nextLine();

        if (USE_DB) {
            try (Connection conn = conectar()) {
                String sql = "INSERT INTO usuarios (nome) VALUES (?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nome);
                stmt.executeUpdate();
                System.out.println("Usuário cadastrado no DB!");
            } catch (Exception e) {
                System.out.println("Erro ao cadastrar no DB: " + e.getMessage());
            }
        } else {
            User u = new User(nextId++, nome);
            memoryUsers.add(u);
            System.out.println("Usuário cadastrado (memória): " + u);
        }
    }

    private static void listar() {
        if (USE_DB) {
            try (Connection conn = conectar()) {
                String sql = "SELECT * FROM usuarios";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                boolean has = false;
                while (rs.next()) {
                    has = true;
                    System.out.println(rs.getInt("id") + " - " + rs.getString("nome"));
                }
                if (!has) System.out.println("Nenhum usuário encontrado.");
            } catch (Exception e) {
                System.out.println("Erro ao listar do DB: " + e.getMessage());
            }
        } else {
            if (memoryUsers.isEmpty()) {
                System.out.println("Nenhum usuário (memória).");
                return;
            }
            for (User u : memoryUsers) {
                System.out.println(u.id + " - " + u.nome);
            }
        }
    }

    private static void atualizar(Scanner sc) {
        System.out.print("ID do usuário: ");
        while (!sc.hasNextInt()) { System.out.println("Digite um número!"); sc.next(); }
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Novo nome: ");
        String nome = sc.nextLine();

        if (USE_DB) {
            try (Connection conn = conectar()) {
                String sql = "UPDATE usuarios SET nome = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nome);
                stmt.setInt(2, id);
                int rows = stmt.executeUpdate();
                if (rows > 0) System.out.println("Atualizado no DB!");
                else System.out.println("ID não encontrado no DB.");
            } catch (Exception e) {
                System.out.println("Erro ao atualizar no DB: " + e.getMessage());
            }
        } else {
            boolean found = false;
            for (User u : memoryUsers) {
                if (u.id == id) {
                    u.nome = nome;
                    found = true;
                    System.out.println("Atualizado (memória)!");
                    break;
                }
            }
            if (!found) System.out.println("ID não encontrado (memória).");
        }
    }

    private static void deletar(Scanner sc) {
        System.out.print("ID do usuário: ");
        while (!sc.hasNextInt()) { System.out.println("Digite um número!"); sc.next(); }
        int id = sc.nextInt();
        sc.nextLine();

        if (USE_DB) {
            try (Connection conn = conectar()) {
                String sql = "DELETE FROM usuarios WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                int rows = stmt.executeUpdate();
                if (rows > 0) System.out.println("Removido do DB!");
                else System.out.println("ID não encontrado no DB.");
            } catch (Exception e) {
                System.out.println("Erro ao deletar no DB: " + e.getMessage());
            }
        } else {
            boolean removed = memoryUsers.removeIf(u -> u.id == id);
            if (removed) System.out.println("Removido (memória)!");
            else System.out.println("ID não encontrado (memória).");
        }
    }

    // classe simples para modo memória
    private static class User {
        int id;
        String nome;
        User(int id, String nome) { this.id = id; this.nome = nome; }
        public String toString() { return id + " - " + nome; }
    }
}
