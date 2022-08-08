package tetris.tools;

import java.util.ArrayList;

/**
 * This class creates an ArrayList, filled with two-dimensional kxk arrays,
 * which depict the k-Stones, so called polyominos either in all embeddings or
 * in the standard-embeddings.
 *
 */
public class TetrominoArraylist {

	public ArrayList<Tetromino> steine;
	private ArrayList<Tetromino> todo;
	private ArrayList<Tetromino> list;

	private Tetromino akt;

	public TetrominoArraylist() {
		steine = new ArrayList<Tetromino>();
		todo = new ArrayList<Tetromino>();
		list = new ArrayList<Tetromino>();
		akt = null;
	}

	/**
	 * This method is responsible for enumerating the standard-embeddings
	 * 
	 * @param k, number of stones
	 * @return steine, all k-standard-embeddings
	 */
	public ArrayList<Tetromino> standardeinbettungenStandardreihenfolgeRek(int k) {

		steine.clear();

		switch (k) {

		case 1:

			steine.add(new Tetromino(1));
			break;

		case 2:
		case 3:
		case 4:
		case 5:
		case 6:

			todo.addAll(standardeinbettungenStandardreihenfolgeRek(k - 1));

			steine.clear();
			
			// for all standardembeddings of k-1
			while (!todo.isEmpty()) {

				// Case 1: add right
				
				// first Stone
				akt = todo.get(0);
				akt = akt.becomeBigger();
				
				// every row from first to last-1
				for (int i = 0; i < k - 1; i++) {
					
					// every column from last-1 to first
					for (int j = k - 2; j >= 0; j--) {
						
						// add one stone to the right if there is a stone already
						if (akt.stein[i][j] == 1) {
							akt.stein[i][j + 1] = 1;
							
							// add the new stone to the k-standardembeddings if it is a new standardembedding
							if (newStandardStone(akt)) {
								addStone(akt);
							}
							
							// first Stone
							akt = todo.get(0);
							akt = akt.becomeBigger();
							break;
						}
					}
				}
				
				// Case 2: add below
				
				// every column from first to last-1
				for (int j = 0; j < k - 1; j++) {
					
					//every row from last-1 to first
					for (int i = k - 2; i >= 0; i--) {
						
						// add one stone below if there is a stone already
						if (akt.stein[i][j] == 1) {
							akt.stein[i + 1][j] = 1;

							// add the new stone to the k-standardembeddings if it is a new standardembedding
							if (newStandardStone(akt)) {
								addStone(akt);
							}

							// first Stone
							akt = todo.get(0);
							akt = akt.becomeBigger();
							break;
						}
					}
				}
				
				// Case 3: add left
				
				// move 1 field to the right so we can add on the left
				akt.moveRight();
				
				// every row from first to last-1
				for (int i = 0; i < k - 1; i++) {
					
					// every column from second to last
					for (int j = 1; j < k; j++) {
						
						// add one stone to the left if there is a stone already
						if (akt.stein[i][j] == 1) {
							akt.stein[i][j - 1] = 1;
							
							// move back to the left if possible
							akt.moveLeftSide();

							// add the new stone to the k-standardembeddings if it is a new standardembedding
							if (newStandardStone(akt)) {
								addStone(akt);
							}
							
							// first Stone, moved to the right
							akt = todo.get(0);
							akt = akt.becomeBigger();
							akt.moveRight();
							break;
						}
					}
				}
				
				// Case 4: add above
				
				// reset Stone
				akt.moveLeftSide();
				
				// move 1 field down, so we can add above
				akt.moveDown();
				
				// every column from first to last-1
				for (int j = 0; j < k-1; j++) {
					
					// every row from second to last
					for (int i = 1; i < k; i++) {
						
						// add one stone to the above if there is a stone already
						if (akt.stein[i][j] == 1) {
							akt.stein[i - 1][j] = 1;
							
							// move back top if possible
							akt.moveTop();

							// add the new stone to the k-standardembeddings if it is a new standardembedding
							if (newStandardStone(akt)) {
								addStone(akt);
							}
							
							// first Stone, moved down
							akt = todo.get(0);
							akt = akt.becomeBigger();
							akt.moveDown();
							break;
						}
					}
				}

				todo.remove(0);
			}
			break;
		case 7:
			
			todo.addAll(standardeinbettungenStandardreihenfolgeRek(k - 1));

			steine.clear();
			
			// for all standardembeddings of k-1
			while (!todo.isEmpty()) {

				// Case 1: add right
				
				// first Stone
				akt = todo.get(0);
				akt = akt.becomeBigger();
				
				// every row from first to last-1
				for (int i = 0; i < k - 1; i++) {
					
					// every column from last-1 to first
					for (int j = k - 2; j >= 0; j--) {
						
						// add one stone to the right if there is a stone already
						if (akt.stein[i][j] == 1) {
							akt.stein[i][j + 1] = 1;
							
							// add the new stone to the k-standardembeddings if it is a new standardembedding and if it has no holes in it
							if (newStandardStone(akt)) {
								if (!akt.hasHole()) {
									addStone(akt);
								}
							}
							
							// first Stone
							akt = todo.get(0);
							akt = akt.becomeBigger();
							break;
						}
					}
				}
				
				// Case 2: add below
				
				// every column from first to last-1
				for (int j = 0; j < k - 1; j++) {
					
					//every row from last-1 to first
					for (int i = k - 2; i >= 0; i--) {
						
						// add one stone below if there is a stone already
						if (akt.stein[i][j] == 1) {
							akt.stein[i + 1][j] = 1;

							// add the new stone to the k-standardembeddings if it is a new standardembedding and if it has no holes in it
							if (newStandardStone(akt)) {
								if (!akt.hasHole()) {
									addStone(akt);
								}
							}

							// first Stone
							akt = todo.get(0);
							akt = akt.becomeBigger();
							break;
						}
					}
				}
				
				// Case 3: add left
				
				// move 1 field to the right so we can add on the left
				akt.moveRight();
				
				// every row from first to last-1
				for (int i = 0; i < k - 1; i++) {
					
					// every column from second to last
					for (int j = 1; j < k; j++) {
						
						// add one stone to the left if there is a stone already
						if (akt.stein[i][j] == 1) {
							akt.stein[i][j - 1] = 1;
							
							// move back to the left if possible
							akt.moveLeftSide();

							// add the new stone to the k-standardembeddings if it is a new standardembedding and if it has no holes in it
							if (newStandardStone(akt)) {
								if (!akt.hasHole()) {
									addStone(akt);
								}
							}
							
							// first Stone, moved to the right
							akt = todo.get(0);
							akt = akt.becomeBigger();
							akt.moveRight();
							break;
						}
					}
				}
				
				// Case 4: add above
				
				// reset Stone
				akt.moveLeftSide();
				
				// move 1 field down, so we can add above
				akt.moveDown();
				
				// every column from first to last-1
				for (int j = 0; j < k-1; j++) {
					
					// every row from second to last
					for (int i = 1; i < k; i++) {
						
						// add one stone to the above if there is a stone already
						if (akt.stein[i][j] == 1) {
							akt.stein[i - 1][j] = 1;
							
							// move back top if possible
							akt.moveTop();

							// add the new stone to the k-standardembeddings if it is a new standardembedding and if it has no holes in it
							if (newStandardStone(akt)) {
								if (!akt.hasHole()) {
									addStone(akt);
								}
							}
							
							// first Stone, moved down
							akt = todo.get(0);
							akt = akt.becomeBigger();
							akt.moveDown();
							break;
						}
					}
				}

				todo.remove(0);
			}
			break;
		}

		return steine;
	}

	/**
	 * This method is responsible for enumerating the all embeddings
	 * 
	 * @param k, number of stones
	 * @return steine, all k-embeddings
	 */
	public ArrayList<Tetromino> alleEinbettungenStandardreihenfolgeRek(int k) {

		steine.clear();

		switch (k) {

		case 1:
			Tetromino b = new Tetromino(1);
			steine.add(b);
			break;

		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:

			todo.addAll(standardeinbettungenStandardreihenfolgeRek(k));
			steine.clear();

			Tetromino c, d, c1, c2, c3, d1, d2, d3 = null;

			while (!todo.isEmpty()) {

				c = todo.get(0);
				addStoneToList(c);

				d = c.mirror();
				if (newListStone(d))
					addStoneToList(d);

				c1 = c.turn();
				if (newListStone(c1))
					addStoneToList(c1);

				c2 = c1.turn();
				if (newListStone(c2))
					addStoneToList(c2);

				c3 = c2.turn();
				if (newListStone(c3))
					addStoneToList(c3);

				d1 = d.turn();
				if (newListStone(d1))
					addStoneToList(d1);

				d2 = d1.turn();
				if (newListStone(d2))
					addStoneToList(d2);

				d3 = d2.turn();
				if (newListStone(d3))
					addStoneToList(d3);

				steine.addAll(list);
				list.clear();
				todo.remove(0);
			}
		}
		return steine;
	}
	
	/**
	 * This method finds the standardembedding of the kStein o
	 * @param o, a kStein
	 * @return min standardembedding
	 */
	private Tetromino findStandardStone(Tetromino o) {
		Tetromino min = o;
		if (o.turn().compareTo(min) == 1) min = o.turn();
		if (o.turn().turn().compareTo(min) == 1) min = o.turn().turn();
		if (o.turn().turn().turn().compareTo(min) == 1) min = o.turn().turn().turn();
		if (o.mirror().compareTo(min) == 1) min = o.mirror();
		if (o.mirror().turn().compareTo(min) == 1) min = o.mirror().turn();
		if (o.mirror().turn().turn().compareTo(min) == 1) min = o.mirror().turn().turn();
		if (o.mirror().turn().turn().turn().compareTo(min) == 1) min = o.mirror().turn().turn().turn();
		
		return min;
	}
	
	/**
	 * This method tells us if a stone with the same standardembedding is inside steine already.
	 * @param o
	 * @return
	 */
	private boolean newStandardStone(Tetromino o) {
		Tetromino min = findStandardStone(o);
		
		for(int i = 0; i < steine.size(); i++) {
			if (steine.get(i).compareTo(min) == 0) return false;
		}
		return true;
	}

	/**
	 * This method tells us if a stone with the same embedding is inside list
	 * already, if that is the case, false will be returned, otherwise true will be
	 * returned.
	 * 
	 * @param o, the stone we want to add to list
	 * @return if a stone with the same embedding is inside list already
	 */
	private boolean newListStone(Tetromino o) {

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).compareTo(o) == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method adds a stone to list and sorts him in it, dependent on his size.
	 * 
	 * @param o, the stone which is added
	 */
	private void addStoneToList(Tetromino o) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).compareTo(o) == -1) {
				list.add(i, o);
				return;
			}
		}
		list.add(o);
	}

	/**
	 * This method adds the standardembedding of a kStein to steine and sorts him in it, dependent on his size.
	 * 
	 * @param o, the stone which is added
	 */
	private void addStone(Tetromino o) {
		Tetromino min = findStandardStone(o);
		
		for (int i = 0; i < steine.size(); i++) {
			if (steine.get(i).compareTo(min) == -1) {
				steine.add(i, min);
				return;
			}
		}
		steine.add(min);
	}

	public Tetromino getRandomStone() {
		Tetromino randomStone = steine.get((int)Math.random()*steine.size()+1);
		return randomStone;
	}

}


