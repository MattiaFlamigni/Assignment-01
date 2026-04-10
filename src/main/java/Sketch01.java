public class Sketch01 {

	
	public static void main(String[] argv) {

		/* 
		 * Different board configs to try:
		 * - minimal: 2 small balls
		 * - large: 400 small balls
		 * - massive: 4500 small balls 
		 */
		
		var boardConf = new MinimalBoardConf();
		// var boardConf = new LargeBoardConf();
		 //var boardConf = new MassiveBoardConf();
		
		Board board = new Board();
		board.init(boardConf);
		
		ViewModel viewModel = new ViewModel();
        GameController controller = new GameController(board, viewModel);
        View view = new View(viewModel, 1200, 800, controller);
        controller.attachView(view);
        controller.start();
	}
}
