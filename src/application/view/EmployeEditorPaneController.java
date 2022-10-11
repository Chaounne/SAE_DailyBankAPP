package application.view;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import application.DailyBankState;
import application.control.ExceptionDialog;
import application.tools.AlertUtilities;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
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
import model.data.Employe;
import model.orm.exception.ApplicationException;
import model.orm.exception.Order;
import model.orm.exception.Table;

public class EmployeEditorPaneController implements Initializable {
	
	// Etat application
		private DailyBankState dbs;

		// Fenêtre physique
		private Stage primaryStage;

		// Données de la fenêtre
		private Employe employeEdite;
		private EditionMode em;
		private Employe employeResult;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	// Manipulation de la fenêtre
		public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
			this.primaryStage = _primaryStage;
			this.dbs = _dbstate;
			this.configure();
		}

		private void configure() {
			this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
		}
		
		/**
		 * 
		 * @param employe
		 * @param mode
		 * @return un employe avec les modifications 
		 */
		public Employe displayDialog(Employe employe, EditionMode mode) {

			this.em = mode;
			if (employe == null) {
				this.employeEdite = new Employe(0, "", "", "", "", "", this.dbs.getEmpAct().idAg);
			} else {
				this.employeEdite = new Employe(employe);
			}
			this.employeResult = null;
			switch (mode) {
			case CREATION:
				this.txtIdemp.setDisable(true);
				this.txtNom.setDisable(false);
				this.txtPrenom.setDisable(false);
				this.rbGuichettier.setSelected(true);
				this.rbChefAgence.setSelected(false);
				this.lblMessage.setText("Informations sur le nouvel employé");
				this.butOk.setText("Ajouter");
				this.butCancel.setText("Annuler");
				break;
			case MODIFICATION:

				this.txtNom.setDisable(false);
				this.txtPrenom.setDisable(false);
				/*
				if ()
				this.rb.setSelected(true);
				this.rbInactif.setSelected(false);
				*/
				this.lblMessage.setText("Informations employé");
				this.butOk.setText("Modifier");
				this.butCancel.setText("Annuler");
				break;
			case SUPPRESSION:
				// ce mode n'est pas utilisé pour les Clients :
				// la suppression d'un client n'existe pas il faut que le chef d'agence
				// bascule son état "Actif" à "Inactif"
				ApplicationException ae = new ApplicationException(Table.NONE, Order.OTHER, "SUPPRESSION EMPLOYE NON PREVUE",
						null);
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();

				break;
			}
			// Paramétrages spécifiques pour les chefs d'agences
			if (ConstantesIHM.isAdmin(this.dbs.getEmpAct())) {
				// rien pour l'instant
			}
			// initialisation du contenu des champs
			this.txtIdemp.setText("" + this.employeEdite.idEmploye);
			this.txtNom.setText(this.employeEdite.nom);
			this.txtPrenom.setText(this.employeEdite.prenom);
			this.txtLogin.setText(this.employeEdite.login);
			this.txtMDP.setText(this.employeEdite.motPasse);

			if (ConstantesIHM.isAdmin(this.employeEdite)) {
				this.rbChefAgence.setSelected(true);
			} else {
				this.rbGuichettier.setSelected(false);
			}

			this.employeResult = null;

			this.primaryStage.showAndWait();
			return this.employeResult;
		}
		
		// Gestion du stage
		private Object closeWindow(WindowEvent e) {
			this.doCancel();
			e.consume();
			return null;
		}
		
		// Attributs de la scene + actions
		@FXML
		private Label lblMessage;
		@FXML
		private TextField txtIdemp;
		@FXML
		private TextField txtNom;
		@FXML
		private TextField txtPrenom;
		@FXML
		private TextField txtLogin;
		@FXML
		private TextField txtMDP;

		@FXML
		private RadioButton rbGuichettier;
		@FXML
		private RadioButton rbChefAgence;
		@FXML
		private ToggleGroup ChefGuichettier;
		@FXML
		private Button butOk;
		@FXML
		private Button butCancel;
		
		/**
		 * Code du bouton d'annulation d'ajout d'un client 
		 */
		@FXML
		private void doCancel() {
			this.employeResult = null;
			this.primaryStage.close();
		}
		
		
		/**
		 * Code du bouton de validation d'ajout d'un client
		 */
		@FXML
		private void doAjouter() {
			switch (this.em) {
			case CREATION:
				if (this.isSaisieValide()) {
					this.employeResult = this.employeEdite;
					this.primaryStage.close();
				}
				break;
			case MODIFICATION:
				if (this.isSaisieValide()) {
					this.employeResult = this.employeEdite;
					this.primaryStage.close();
				}
				break;
			case SUPPRESSION:
				this.employeResult = this.employeEdite;
				this.primaryStage.close();
				break;
			}

		}

		
		/**
		 * Méthode qui vérifie la saisie d'un client 
		 * @return retourne si la saisie d'un client est correcte ou pas  
		 */
		private boolean isSaisieValide() {
			this.employeEdite.nom = this.txtNom.getText().trim();
			this.employeEdite.prenom = this.txtPrenom.getText().trim();
			this.employeEdite.login = this.txtLogin.getText().trim();
			this.employeEdite.motPasse = this.txtMDP.getText().trim();
			
			if (this.rbChefAgence.isSelected()) {
				this.employeEdite.droitsAccess = ConstantesIHM.AGENCE_CHEF;
			} else {
				this.employeEdite.droitsAccess = ConstantesIHM.AGENCE_GUICHETIER;
			}

			if (this.employeEdite.nom.isEmpty()) {
				AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le nom ne doit pas être vide",
						AlertType.WARNING);
				this.txtNom.requestFocus();
				return false;
			}
			if (this.employeEdite.prenom.isEmpty()) {
				AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le prénom ne doit pas être vide",
						AlertType.WARNING);
				this.txtPrenom.requestFocus();
				return false;
			}

			return true;
		}
		

}
