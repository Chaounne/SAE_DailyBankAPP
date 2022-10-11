package application.view;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import application.control.OperationEditorPane;
import application.control.Simulation;
import application.tools.AlertUtilities;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Operation;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;

public class SimulationController implements Initializable {

	// Etat application
	private Simulation sm;
	
	// Fenêtre physique
	private Stage primaryStage;
	
	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, Simulation _sm) {
		this.sm = _sm;
		this.primaryStage = _primaryStage;
		this.configure();
	}	
		
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
		this.validateComponentState();
	}
	
	public void displayDialog() {
		this.primaryStage.showAndWait();
	}
	
	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}
	
	// Attributs de la scene + actions
	@FXML
	private Label lblTauxAssurance;
	@FXML 
	private TextField txtCapital;
	@FXML
	private TextField txtDuree;
	@FXML
	private TextField txtTauxInteret;
	@FXML
	private TextField txtTauxAssurance;
	@FXML
	private RadioButton btnAssurance;
	@FXML
	private RadioButton btnSansAssurance;
	@FXML
	private Button btnSimuler;
	@FXML
	private Button btnAnnuler;
	@FXML
	private ToggleGroup group;
			
	@Override
	public void initialize(URL location, ResourceBundle resources) {
				
	}
	
	/**
	 * Permet de fermer l'interface
	 */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}
	
	@FXML
	private void doSimulation() throws FileNotFoundException, DataAccessException, DatabaseConnexionException, DocumentException {
		if(this.isSaisieValide()) {
			imprimer();
			this.primaryStage.close();
			
		}
	}
public void imprimer() throws FileNotFoundException, DocumentException, DataAccessException, DatabaseConnexionException {
				
		Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream("simulation.pdf"));

		document.open();
		
             double capital = Double.parseDouble(this.txtCapital.getText().toString());
             int duree = Integer.parseInt(this.txtDuree.getText().toString());
             double tauxinteret = Double.parseDouble(this.txtTauxInteret.getText().toString());
             
             double mensualite = capital*((tauxinteret/100/12)/(1-Math.pow(1+tauxinteret/100/12, -duree*12)));
             
             boolean assurance = false;
             if(this.group.getSelectedToggle() == this.btnAssurance) {
                 double tauxassurance = Double.parseDouble(this.txtTauxAssurance.getText().toString());
                 mensualite = mensualite + (tauxassurance*capital/100/12);
                 assurance = true;
             }
                 
             
            
	
		
		
		String paragraphe ="";
		Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
		for (int i =1; i<=(duree*12);i++) {
			paragraphe+=i+"   "+capital+"   "+mensualite+"   "+assurance;
			capital = capital - mensualite;
			paragraphe +="   "+capital+"\n";
		}
			

			
			
		
		
		
		Paragraph chunk = new Paragraph(paragraphe);
		document.add(chunk);
		
		document.close();
	}
		
	private boolean isSaisieValide() {
		
		String regex = "[0-9]";
		String regex2 = "^\\d+(\\.\\d+)";
			
		if(this.txtCapital.getText().trim().isEmpty() || (!Pattern.matches(regex, this.txtCapital.getText().trim()) && this.txtCapital.getText().trim().length() > 6)) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le capital doit �tre renseign� ou n'est pas valide",
					AlertType.WARNING);
			this.txtCapital.requestFocus();
			return false;
		}
		if(this.txtDuree.getText().trim().isEmpty() || (!Pattern.matches(regex, this.txtDuree.getText().trim()) && this.txtDuree.getText().trim().length() > 6)) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "La dur�e doit �tre renseign�e ou n'est pas valide",
					AlertType.WARNING);
			this.txtDuree.requestFocus();
			return false;
		}
		if(this.txtTauxInteret.getText().trim().isEmpty() || !Pattern.matches(regex2, this.txtTauxInteret.getText().trim())) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le taux d'int�r�t doit �tre renseign� ou n'est pas valide",
					AlertType.WARNING);
			this.txtTauxInteret.requestFocus();
			return false;
		}
		if(this.group.getSelectedToggle() == null) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de renseignement", null, "Indiquez si vous avez une assurance ou non",
					AlertType.WARNING);
			return false;
		}
		if(this.group.getSelectedToggle() == this.btnAssurance) {
			if(this.txtTauxAssurance.getText().trim().isEmpty() || !Pattern.matches(regex2, this.txtTauxAssurance.getText().trim())) {
				AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le taux d'assurance doit être valide",
						AlertType.WARNING);
				this.txtTauxAssurance.requestFocus();
				return false;
			}
			
		}
				
		return true;
	}
	
	/**
	 * Permet de d'activer/desactiver les boutons de manière logique en fonction de l'état de certains facteurs (si le client est assur� ou non)
	 */
	private void validateComponentState() {	
		
		this.lblTauxAssurance.setDisable(false);
		this.txtTauxAssurance.setDisable(false);
		
		//set "lblTauxAssurance and txtTauxAssurance" to disabled state each time "btnSansAssurance" radiobutton is selected
		this.lblTauxAssurance.disableProperty().bind(this.btnSansAssurance.selectedProperty());
		this.txtTauxAssurance.disableProperty().bind(this.btnSansAssurance.selectedProperty());
	}
	
}
