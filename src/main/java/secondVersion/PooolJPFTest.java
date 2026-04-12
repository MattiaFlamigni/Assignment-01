package secondVersion;

public class PooolJPFTest {
    public static void main(String[] args) {
        // Creiamo la Board
        Board board = new Board();

        // Creiamo le posizioni e velocità usando i record corretti
        P2d p1 = new P2d(0, 0);
        V2d v1 = new V2d(1.0, 0.0);

        P2d p2 = new P2d(0.1, 0); // Molto vicina alla prima
        V2d v2 = new V2d(-1.0, 0.0);

        double radius = 0.1;
        double mass = 1.0;

        // Creiamo due palline (Small Ball)
        // Nota: Assicurati che i parametri corrispondano al tuo costruttore di Ball
        Ball b1 = new Ball(p1, radius, mass, v1, Ball.Type.SMALL);
        Ball b2 = new Ball(p2, radius, mass, v2, Ball.Type.SMALL);

        // Simuliamo due thread che tentano di risolvere la stessa collisione
        // Questo è lo scenario tipico che JPF analizza per trovare Deadlock
        Thread t1 = new Thread(() -> {
            // Usiamo il metodo che abbiamo scritto nella Board
            // resolveBallPair contiene i synchronized nidificati
            board.resolveBallPair(b1, b2);
        });

        Thread t2 = new Thread(() -> {
            board.resolveBallPair(b2, b1); // Ordine invertito per forzare il controllo
        });

        t1.start();
        t2.start();
    }
}