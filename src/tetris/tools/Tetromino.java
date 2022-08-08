package tetris.tools;

/**
 * This class creates the k-Stones needed for the ArrayList in kSteine.
 *
 */
public class Tetromino implements Comparable <Tetromino>{
	
	public int[][] stein;
	private int k;

	/**
	 * Constructor of kStein: Creates a kStein eiter 1x1 and filled or kxk and not filled.
	 * @param k, the size of the kStein
	 */
	public Tetromino(int k){
		this.k = k;
		
		stein = new int[k][k];
		
		if (k == 1) {
			stein[0][0] = 1;
		} else {
			for (int i = 0; i < k; i++) {
				for (int j = 0; j < k; j++) {
					stein[i][j] = 0;
				}
			}
		}
	}
	
	/**
	 * turns a kxk kStein into a k+1xk+1 kStein
	 * @return the k+1xk+1 kStein
	 */
	public Tetromino becomeBigger() {
		Tetromino a = new Tetromino(k+1);
		for(int i = 0; i < k; i++) {
			for (int j = 0; j < k; j++) {
				a.stein[i][j] = stein[i][j];
			}
			a.stein[i][k] = a.stein[k][i] = 0;
		}
		return a;
	}
	
	/**
	 * moves a kStein 1 bit to the right
	 */
	public void moveRight() {
		for(int i = 0; i < k; i++) {
			for(int j = k-2; j >= 0; j--) {
				stein[i][j+1] = stein[i][j]; 
			}
			stein[i][0] = 0;
		}
	}
	
	/**
	 * moves a kStein 1 bit down
	 */
	public void moveDown() {
		for(int i = 0; i < k; i++) {
			for(int j = k-2; j >= 0; j--) {
				stein[j+1][i] = stein[j][i]; 
			}
			stein[0][i] = 0;
		}
	}
	
	/**
	 * checks the first row of the kStein
	 * @return true if its empty
	 */
	private boolean checkTop() {
		for(int i = 0; i < stein[0].length; i++) {
			if(stein[0][i] == 1) return false;
		} return true;
	}
	
	/**
	 * moves a kStein to the Top so a part of it is in the first row
	 */
	public void moveTop() {
		while(checkTop()) {
			for(int i = 0; i < k; i++) {
				for(int j = 1; j < k; j++) {
					stein[j-1][i] = stein[j][i]; 
				}
				stein[k-1][i] = 0;
			}
		}
	}
	
	/**
	 * checks the first column of the kStein
	 * @return true if its empty
	 */
	private boolean checkLeft() {
		for(int i = 0; i < stein.length; i++) {
			if(stein[i][0] == 1) return false;
		} return true;
	}
	
	/**
	 * moves a kStein to the Left so a part of it is in the first row
	 */
	public void moveLeftSide() {
		while(checkLeft()) {
			for(int i = 0; i < k; i++) {
				for(int j = 1; j < k; j++) {
					stein[i][j-1] = stein[i][j]; 
				}
				stein[i][k-1] = 0;
			}
		}
	}
	
	/**
	 * copies a kStein
	 * @return the copied kStein
	 */
	private Tetromino copyStone() {
		Tetromino b = new Tetromino(k);
		
		for(int i = 0; i < stein.length; i++) {
			for (int j = 0; j < stein[i].length; j++) {
				b.stein[i][j] = stein[i][j];
			}
		}
		return b;
	}
	
	/**
	 * turns a kStein 90 degrees to the right
	 * @return turned kStein
	 */
	public Tetromino turn() {
		int temp;
		Tetromino b = new Tetromino(k);
		b = copyStone();
		for (int i = 0; i < k/2; i++) {
			for (int j = 0; j < k-(i*2+1); j++) {
				temp = b.stein[i][i+j];
				b.stein[i][i+j] = b.stein[k-i-j-1][i];
				b.stein[k-1-i-j][i] = b.stein[k-i-1][k-i-j-1];
				b.stein[k-i-1][k-i-j-1] = b.stein[i+j][k-1-i];
				b.stein[i+j][k-1-i] = temp;
			}
		}
		b.moveTop();
		b.moveLeftSide();
		return b;
	}
	
	/**
	 * mirrors a kStein
	 * @return the mirrored kStein
	 */
	public Tetromino mirror() {
		
		Tetromino b = new Tetromino(k);
		
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < k; j++) {
				b.stein[i][j] = stein[i][k-1-j];
			}
		}
		
		b.moveTop();
		b.moveLeftSide();
		return b;	
	}
	
	/**
	 * mirrors a kStein on the horicontal axis
	 * @return the mirrored kStein
	 */
	public Tetromino mirror2() {
		
		Tetromino b = new Tetromino(k);
		
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < k; j++) {
				b.stein[i][j] = stein[k-1-i][j];
			}
		}
		
		b.moveTop();
		b.moveLeftSide();
		return b;	
	}

	/**
	 * compares one kStein to another
	 * @return 1 if o is greater, -1 if o is smaller and 0 if they're the same
	 * @param o, a kStein
	 */
	@Override
	public int compareTo(Tetromino o) {
			
		for(int i = 0; i < stein.length; i++) {
			for (int j = 0; j < stein.length; j++) {
				if(this.stein[i][j] < o.stein[i][j]) {
					return -1;
				} 
				else if (this.stein[i][j] > o.stein[i][j]) {
					return 1;
				}
			}
		}
		return 0;
	}
	
	/**
	 * Returns the stein array of a kStein object which contains the structure of the stone.
	 * @return the stein array which contains the structure of the stone
	 */
	public int[][] getStein()
	{
		return this.stein;
	}

	/**
	 * Returns whether the kStein has a 1x1 hole.
	 * @return true if the kStein has a 1x1 hole. False if not.
	 */
	public boolean hasHole() {
		for(int i = 1; i < k-1; i++) {
			for (int j = 1; j < k-1; j++) {
				if (stein[i][j] == 0) {
					if (numberNeighbours(i, j) == 4) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns the number of neighbors a kachel has.
	 * @param i, the row
	 * @param j, the column
	 * @return the number of neighbors
	 */
	public int numberNeighbours(int i, int j) {
		return stein[i-1][j] + stein[i+1][j] + stein[i][j-1] + stein[i][j+1];
	}
	
	/**
	 * Returns whether the kStein has a 2x1 hole.
	 * @return true if the kStein has a 2x1 hole. False if not.
	 */
	public boolean hasBigHole() {
		for(int i = 1; i < k-1; i++) {
			for (int j = 1; j < k-1; j++) {
				if (stein[i][j] == 0) {
					if(numberNeighbours(i, j) == 3) {
						if(checkNeighbourhole(i, j)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns if the neighbour is part of the 2x1 hole
	 * @param i, the row
	 * @param j, the column
	 * @return true, if it is part of the the hole, false if not.
	 */
	public boolean checkNeighbourhole(int i, int j) {
		if (stein[i+1][j] == 0) {
			if(checkBounds(i+1,j)) {
				return numberNeighbours(i+1, j) == 3;
			}
		} else if (stein[i-1][j] == 0) {
			if(checkBounds(i-1,j)) {
				return numberNeighbours(i-1, j) == 3;
			}
		} else if (stein[i][j+1] == 0) {
			if(checkBounds(i,j+1)) {
				return numberNeighbours(i, j+1) == 3;
			}
		} else if (stein[i][j-1] == 0) {
			if(checkBounds(i,j-1)) {
				return numberNeighbours(i, j-1) == 3;
			}
		}
		return false;
	}
	
	/**
	 * returns if the neighbour can be a hole
	 * @param i, the row
	 * @param j, the column
	 * @return true, if it can be a hole, false if not.
	 */
	public boolean checkBounds(int i, int j) {
		return (i < k-1 && i > 0 && j < k-1 && j > 0);
	}
}
