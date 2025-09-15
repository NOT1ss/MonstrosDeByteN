package FM;

import robocode.*;
import java.awt.Color;
import java.util.Random;

public class TrystanBot extends AdvancedRobot {

    private Random aleatorio = new Random();
    private byte direcaoMovimento = 1; // Usado para alternar a direção

    public void run() {
        // Define as cores do robô
        setColors(new Color(128, 0, 128), Color.black, Color.blue);

        // Permite que o radar, a arma e o corpo do robô se movam de forma independente
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);

        // Loop principal: A única responsabilidade é procurar inimigos sem parar.
        // As chamadas set... são não-bloqueantes. O execute() no final aplica todas elas.
        while (true) {
            setTurnRadarRight(360); // Gira o radar para escanear
            execute(); // Executa todas as ações pendentes (neste caso, o giro do radar)
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent evento) {
        // --- LÓGICA DE MIRA E TIRO ---
        double distancia = evento.getDistance();
        double potenciaDeTiro = Math.min(3, Math.max(1, 400 / distancia));

        // Mira preditiva simples
        double bearingAbsoluto = getHeadingRadians() + evento.getBearingRadians();
        double giroDoCanhao = robocode.util.Utils.normalRelativeAngle(bearingAbsoluto - getGunHeadingRadians());
        setTurnGunRightRadians(giroDoCanhao);

        // Atira se a arma não estiver quente
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(potenciaDeTiro);
        }
        
        // --- LÓGICA DE MOVIMENTO EVASIVO ---
        // Vira 90 graus em relação ao inimigo e se move
        setTurnRight(evento.getBearing() + 90);
        
        // Lógica para alternar entre ir para frente e para trás para ser imprevisível
        if (getTime() % 20 == 0) {
            direcaoMovimento *= -1;
            setAhead(150 * direcaoMovimento);
        }
        
        // --- LÓGICA DO RADAR ---
        // Mantém o radar travado no inimigo que acabamos de ver
        setTurnRadarRight(getHeading() - getRadarHeading() + evento.getBearing());

        // Executa todas as ações planejadas (virar, mover, atirar, etc.)
        execute();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent evento) {
        // Ao ser atingido, tenta sair da linha de tiro
        setTurnRight(evento.getBearing() + 90);
        setAhead(150);
        execute();
    }

    @Override
    public void onHitWall(HitWallEvent evento) {
        // Se bater na parede, recua e vira
        direcaoMovimento *= -1; // Inverte a direção de movimento
        setAhead(100 * direcaoMovimento);
        execute();
    }
}