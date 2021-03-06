package battlegame;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3498870086529871294L;
	ArrayList<Unit> units;
	Color color;
	
	Player(Color color) {
		this.color = color;

		units = new ArrayList<Unit>();
		generateUnits();
	}
	
	public void update(double delta) {
		for (Unit unit : units) {
			unit.update(delta);
		}
	}

	private void generateUnits() {
		for (int i = 0; i < 10; i++) {
			units.add(new Unit());
		}
	}

	public void paint(Graphics g) {
		for (Unit unit : units)
			unit.paint(g, color);
	}

	public void attack(double delta, ArrayList<Player> players) {
		for (Player player : players)
			if (player != this)
				for (Unit myUnit : this.units)
					for (Unit theirUnit : player.units)
						myUnit.damage(delta, theirUnit);
	}

	public void paint(Graphics g, ArrayList<Integer> selection) {
		for (Unit unit : units)
			unit.paint(g, color, selection);
	}
}
