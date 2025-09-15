package FM;

import robocode.*;
import java.awt.Color;
import java.util.Random;

public class TrystanBot extends AdvancedRobot {

    private byte direcaoMovimento = 1; // Usado para alternar a direção

    public void run() {
        // Define as cores do robô
        setColors(new Color(128, 0, 128), Color.black, Color.blue);

        // Permite que o radar, a arma e o corpo do robô se movam de forma independente
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);

        // Loop principal definitivo: Apenas gira o radar. O motor do jogo chama execute() automaticamente.
        // Isso garante que o robô está sempre receptivo a eventos como onScannedRobot.
        while (true) {
            turnRadarRight(360);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent evento) {
        // --- DIAGNÓSTICO: Imprime no log da batalha para confirmar que o evento foi acionado ---
        out.println("TrystanBot VIU O INIMIGO: " + evento.getName() + " no turno " + getTime());

        // --- LÓGICA DE MIRA E TIRO ---
        double distancia = evento.getDistance();
        double potenciaDeTiro = Math.min(3, Math.max(1, 400 / distancia));

        double bearingAbsoluto = getHeadingRadians() + evento.getBearingRadians();
        double giroDoCanhao = robocode.util.Utils.normalRelativeAngle(bearingAbsoluto - getGunHeadingRadians());
        
        // Usamos setTurnGunRightRadians para ser não-bloqueante
        setTurnGunRightRadians(giroDoCanhao);

        // Atira se a arma não estiver quente
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(potenciaDeTiro);
        }
        
        // --- LÓGICA DE MOVIMENTO EVASIVO ---
        setTurnRight(evento.getBearing() + 90);
        
        if (getTime() % 20 == 0) {
            direcaoMovimento *= -1;
            setAhead(150 * direcaoMovimento);
        }
        
        // Não precisamos mais girar o radar aqui, o loop run() já faz isso.
        // Isso evita que o radar fique "preso" em um inimigo e perca outros.
    }

    @Override
    public void onHitByBullet(HitByBulletEvent evento) {
        // Ao ser atingido, tenta sair da linha de tiro
        setTurnRight(evento.getBearing() + 90);
        setAhead(150);
    }

    @Override
    public void onHitWall(HitWallEvent evento) {
        // Se bater na parede, recua e vira
        direcaoMovimento *= -1;
        setAhead(100 * direcaoMovimento);
    }
}