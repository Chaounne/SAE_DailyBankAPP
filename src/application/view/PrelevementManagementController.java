package application.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.PrelevementEditorPane;
import application.control.PrelevementManagement;
import application.tools.EditionMode;
import application.tools.NoSelectionModel;
import application.tools.PairsOfValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.PrelevementAutomatique;
import model.orm.AccessPrelevement;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.RowNotFoundOrTooManyRowsException;

public class PrelevementManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private PrelevementManagement pm;
	private PrelevementEditorPane ped;
	private AccessPrelevement accp = new AccessPrelevement();

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenÃªtre
	private Client clientDuCompte;
	private CompteCourant compteConcerne;
	private ObservableList<PrelevementAutomatique> olPrelevement;

	// Manipulation de la fenÃªtre
	public void initContext(Stage _primaryStage, PrelevementManagement _pm, DailyBankState _dbstate, Client client, CompteCourant compte) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.pm = _pm;
		this.clientDuCompte = client;
		this.compteConcerne = compte;
		this.configure();
	}

	private void configure() {
		
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olPrelevement = FXCollections.observableArrayList();
		
		this.loadList();
		this.lvPrelevements.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvPrelevements.getFocusModel().focus(-1);
		this.lvPrelevements.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
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
	private Label lblInfosClient;
	@FXML
	private Label lblInfosCompte;
	@FXML
	private ListView<PrelevementAutomatique> lvPrelevements;
	@FXML
	private Button btnEnre;
	@FXML
	private Button btnSuppr;
	@FXML
	private Button btnModif;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}
	
	@FXML
	private void doEnregistrer() {
		this.ped = new PrelevementEditorPane(primaryStage, dbs);
		int selectedIndice = this.lvPrelevements.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			PrelevementAutomatique prlv = this.lvPrelevements.getItems().get(selectedIndice);
			this.ped.doPrelevementEditorDialog(compteConcerne, prlv, EditionMode.CREATION);
		}
		else {
			this.ped.doPrelevementEditorDialog(compteConcerne, null, EditionMode.CREATION);
		}
		this.loadList();
	}
	
	@FXML
	private void doModif() {
		this.ped = new PrelevementEditorPane(primaryStage, dbs);
		int selectedIndice = this.lvPrelevements.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			PrelevementAutomatique prlv = this.lvPrelevements.getItems().get(selectedIndice);
			this.ped.doPrelevementEditorDialog(compteConcerne, prlv, EditionMode.MODIFICATION);
		}
		this.loadList();
		this.validateComponentState();
	}
	
	@FXML
	private void doSuppr() {
		int selectedIndice = this.lvPrelevements.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			PrelevementAutomatique prlv = this.lvPrelevements.getItems().get(selectedIndice);
			try {
				this.accp.deletePrelev(prlv);
			} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.loadList();
		this.validateComponentState();
	}



	private void loadList() {
		this.lvPrelevements.getItems().clear();
		try {
			System.out.println(accp.getPrelevements(this.compteConcerne.idNumCompte));
			int size = accp.getPrelevements(this.compteConcerne.idNumCompte).size();
			for(int i = 0; i < size; i = i + 1) {
				System.out.println(accp.getPrelevements(this.compteConcerne.idNumCompte).get(i).toString());
				this.olPrelevement.add(accp.getPrelevements(this.compteConcerne.idNumCompte).get(i));
				this.lvPrelevements.getItems().add(accp.getPrelevements(this.compteConcerne.idNumCompte).get(i));
			}
		} catch (DataAccessException e1) {
			
		} catch (DatabaseConnexionException e1) {
			
		}
	}
	
	private void validateComponentState() {
		this.btnEnre.setDisable(false);
		this.btnSuppr.setDisable(false);
		this.btnModif.setDisable(false);
	
	}

	
}
