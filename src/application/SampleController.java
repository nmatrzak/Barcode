package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class SampleController implements Initializable {

	@FXML
	private ImageView imageView;
	@FXML
	private Label label;
	@FXML
	private Label labelWstaw;
	@FXML
	private Label labelOdczyt;
	@FXML
	private TextField text;
	@FXML
	private Button button;
	@FXML
	private Button buttonWstaw;
	@FXML
	private Button buttonDekoduj;
	@FXML
	private Stage stage;

	private File plik;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	@FXML
	public void handle() throws Exception {

		String msg = text.getText();
		try {

			Code39Bean bean = new Code39Bean();
			int dpi = 200;
			bean.setModuleWidth(UnitConv.in2mm(1.0f / dpi));
			bean.setWideFactor(3);
			bean.doQuietZone(true);
			File outputFile = new File("ppp.png");
			OutputStream out = new FileOutputStream(outputFile);
			try {
				BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, "image/png", dpi,
						BufferedImage.TYPE_BYTE_GRAY, false, 0);
				bean.generateBarcode(canvas, msg);
				// Signal end of generation
				canvas.finish();
				FileInputStream input = new FileInputStream(
						"/Users/nmatr/eclipse-workspace/ProjekPrzjscia/Generator&ReaderBarCode2/ppp.png");
				Image image = new Image(input);
				imageView = new ImageView(image);
				label.setGraphic(imageView);

			} finally {
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// BufferedImage barcodeImage = canvas.getBufferedImage();
		// Image image = SwingFXUtils.toFXImage(barcodeImage, null);
		// ImageView barImageView = new ImageView(image);
		// label = new Label(null, barImageView);
		// canvas.finish();

	}

	@FXML
	public void handleButtonWczytaj() throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Otwórz Plik");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Pliki PNG", "*.png"));
		plik = fileChooser.showOpenDialog(stage);

		if (plik != null) {

			FileInputStream input = new FileInputStream(plik.getCanonicalPath());
			Image image = new Image(input);
			imageView = new ImageView(image);
			labelWstaw.setGraphic(imageView);

		}
	}

	@FXML
	public void handleButtonDecode() throws Exception {

		String path = plik.getAbsolutePath();
		InputStream inputStream = new FileInputStream(path);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Reader reader = new MultiFormatReader();
		Result result = reader.decode(bitmap);
		labelOdczyt.setText(result.getText());
		System.out.println("Decoded Code of " + path + "is :" + result.getText());
	}
}