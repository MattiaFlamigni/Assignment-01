package secondVersion;

public class View {

	private final ViewFrame frame;
	private final ViewModel viewModel;
	
	public View(ViewModel model, int w, int h, HumanInputListener listener) {
		frame = new ViewFrame(model, w, h, listener);
		frame.setVisible(true);
		this.viewModel = model;
	}
		
	public void render() {
		frame.render();
	}
	public ViewModel getViewModel() {
		return viewModel;
	}
}
