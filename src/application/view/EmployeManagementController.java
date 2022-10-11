package application.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.EmployeManagement;
import application.tools.ConstantesIHM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.Employe;

public class EmployeManagementController implements Initializable {
	
	private DailyBankState dbs;
	private EmployeManagement em;
	
	private Stage primaryStage;

	private ObservableList<Employe> olc;
	
	// Manipulation de la fenêtre
		public void initContext(Stage _primaryStage, EmployeManagement _em, DailyBankState _dbstate) {
			this.em = _em;
			this.primaryStage = _primaryStage;
			this.dbs = _dbstate;
			this.configure();
		}
		
		private void configure() {
			this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

			this.olc = FXCollections.observableArrayList();
			this.lvEmployes.setItems(this.olc);
			this.lvEmployes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			this.lvEmployes.getFocusModel().focus(-1);
			this.lvEmployes.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
			this.validateComponentState();
		}
		
		public void displayDialog() {
			this.primaryStage.showAndWait();
		}
		
		// Gestion du stage
		private Object closeWindow(WindowEvent e) {
			this.doCancel2();
			e.consume();
			return null;
		}
		
		@FXML
		private TextField txtNum;
		@FXML
		private TextField txtNom;
		@FXML
		private TextField txtPrenom;
		@FXML
		private ListView<Employe> lvEmployes;
		@FXML
		private Button btnSupprEmploye;
		@FXML
		private Button btnModifEmploye;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	
	@FXML
	private void doCancel2() {
		this.primaryStage.close();
	}
	
	@FXML
	private void doRechercherEmp() {
		int numEmploye;
		try {
			String nc = this.txtNum.getText();
			if (nc.equals("")) {
				numEmploye = -1;
			} else {
				numEmploye = Integer.parseInt(nc);
				if (numEmploye < 0) {
					this.txtNum.setText("");
					numEmploye = -1;
				}
			}
		} catch (NumberFormatException nfe) {
			this.txtNum.setText("");
			numEmploye = -1;
		}

		String debutNom = this.txtNom.getText();
		String debutPrenom = this.txtPrenom.getText();

		if (numEmploye != -1) {
			this.txtNom.setText("");
			this.txtPrenom.setText("");
		} else {
			if (debutNom.equals("") && !debutPrenom.equals("")) {
				this.txtPrenom.setText("");
			}
		}

		// Recherche des clients en BD. cf. AccessClient > getClients(.)
		// numCompte != -1 => recherche sur numCompte
		// numCompte != -1 et debutNom non vide => recherche nom/prenom
		// numCompte != -1 et debutNom vide => recherche tous les clients
		ArrayList<Employe> listeEmp;
		listeEmp = this.em.getlisteEmployes(numEmploye, debutNom, debutPrenom);

		this.olc.clear();
		for (Employe emp : listeEmp) {
			this.olc.add(emp);
		}

		this.validateComponentState();
	}
	
	private void validateComponentState() {
		// Non implémenté => désactivé
		this.btnSupprEmploye.setDisable(true);
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			this.btnModifEmploye.setDisable(false);
			this.btnSupprEmploye.setDisable(false);
		} else {
			this.btnModifEmploye.setDisable(true);
			this.btnSupprEmploye.setDisable(true);
		}
	}
	
	@FXML
	private void doNouvelEmploye() {
		Employe employe;
		employe = this.em.nouvelEmploye();
		if (employe != null) {
			this.olc.add(employe);
		}
	}
	
	@FXML
	private void doModifierEmploye() {

		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Employe EmpMod = this.olc.get(selectedIndice);
			Employe result = this.em.modifierEmploye(EmpMod);
			if (result != null) {
				this.olc.set(selectedIndice, result);
			}
		}
	}
	
	@FXML
	private void doSupprimerEmploye() {
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Employe EmpMod = this.olc.get(selectedIndice);
			if(ConstantesIHM.isAdmin(EmpMod)==false) {
				this.em.supprimerEmploye(EmpMod);
				this.doRechercherEmp();
			}
		}
	}
}
