package application.view;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.PrelevementAutomatique;
import model.orm.AccessPrelevement;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.ManagementRuleViolation;
import model.orm.exception.RowNotFoundOrTooManyRowsException;

public class PrelevementEditorPaneController implements Initializable {

	// Etat application
	private DailyBankState dbs;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private EditionMode em;
	private Client clientDuCompte;
	private CompteCourant compte;
	private PrelevementAutomatique prelevementEdite;
	private PrelevementAutomatique prelevementResult;
	private AccessPrelevement accp = new AccessPrelevement();

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
		
		
	}

	public PrelevementAutomatique displayDialog(CompteCourant Compte, PrelevementAutomatique prlv, EditionMode mode) {
		this.em = mode;
		this.compte = Compte;
		if (prlv == null) {
			this.prelevementEdite = new PrelevementAutomatique(0, 0, "0", " ", compte.idNumCompte);

		} else {
			this.prelevementEdite = new PrelevementAutomatique(prlv);
		}
		this.prelevementResult = null;
		this.txtIdPrelev.setDisable(true);
		this.txtBeneficiaire.setDisable(true);
		this.txtIdNumCompte.setDisable(true);
		switch (mode) {
		case CREATION:
			this.txtBeneficiaire.setDisable(false);
			this.txtSolde.setDisable(false);
			this.lblMessage.setText("Informations sur le pr�l�vement");
			this.lblSolde.setText("Montant");
			this.btnOk.setText("Ajouter");
			this.btnCancel.setText("Annuler");
			break;
		case MODIFICATION:
			this.txtBeneficiaire.setDisable(false);
			this.txtSolde.setDisable(false);
			this.lblMessage.setText("Informations sur le pr�l�vement");
			this.lblSolde.setText("Montant");
			this.btnOk.setText("Modifier");
			this.btnCancel.setText("Annuler");
			//this.txtBeneficiaire.focusedProperty().addListener((t, o, n) -> this.focusBeneficiaire(t, o, n));
			//this.txtDate.focusedProperty().addListener((t, o, n) -> this.focusDate(t, o, n));
			//this.txtSolde.focusedProperty().addListener((t, o, n) -> this.focusMontant(t, o, n));
			break;
		case SUPPRESSION:
			AlertUtilities.showAlert(this.primaryStage, "Non implémenté", "Suppression de compte n'est pas implémenté",
					null, AlertType.ERROR);
			return null;
		// break;
		}

		// initialisation du contenu des champs
		this.txtIdPrelev.setText("" + this.prelevementEdite.idPrelev);
		this.txtIdNumCompte.setText("" + this.prelevementEdite.idNumCompte);
		this.txtBeneficiaire.setText("" + this.prelevementEdite.beneficiaire);
		this.txtDate.setText("" + this.prelevementEdite.dateRecurrente);
		this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.prelevementEdite.montant));

		this.prelevementResult = null;

		this.primaryStage.showAndWait();
		return this.prelevementResult;
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	private Object focusMontant(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				int val;
				val = Integer.parseInt(this.txtSolde.getText().trim());
				if (val < 0) {
					throw new NumberFormatException();
				}
				this.prelevementEdite.montant = val;
			} catch (NumberFormatException nfe) {
				this.txtSolde.setText("" + this.prelevementEdite.montant);
			}
		}
		return null;
	}

	private Object focusBeneficiaire(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				String val;
				val = this.txtBeneficiaire.getText().trim();
				if (val == null) {
					throw new NumberFormatException();
				}
				this.prelevementEdite.beneficiaire = val;
			} catch (NumberFormatException nfe) {
				this.txtBeneficiaire.setText("" + this.prelevementEdite.beneficiaire);
			}
		}
		return null;
	}
	
	//private Object focusDate(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
		//	boolean newPropertyValue) {
		//if (oldPropertyValue) {
			//try {
				//String val;
				//val = this.txtSolde.getText().trim();
				//if (val == null) {
					//throw new NumberFormatException();
				//}
				//this.prelevementEdite.dateRecurrente = val;
			//} catch (NumberFormatException nfe) {
				//this.txtDate.setText(String.format(Locale.ENGLISH, "%10.02f", this.prelevementEdite.dateRecurrente));
			//}
		//}
		//this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.prelevementEdite.dateRecurrente));
		//return null;
	//}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private Label lblSolde;
	@FXML
	private TextField txtIdPrelev;
	@FXML
	private TextField txtBeneficiaire;
	@FXML
	private TextField txtIdNumCompte;
	@FXML
	private TextField txtDate;
	@FXML
	private TextField txtSolde;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.prelevementResult = null;
		this.primaryStage.close();
	}

	@FXML
	private void doAjouter() {
		switch (this.em) {
		case CREATION:
			if (this.isSaisieValide()) {
				this.prelevementResult = this.prelevementEdite;
				this.prelevementResult.beneficiaire = this.txtBeneficiaire.getText();
				this.prelevementResult.montant = Double.parseDouble(this.txtSolde.getText());
				System.out.println(this.prelevementResult.montant);
				this.prelevementResult.dateRecurrente = this.txtDate.getText();
				try {
					this.accp.ajouterPrelevementAuto(this.prelevementResult.montant, this.prelevementResult.dateRecurrente, this.prelevementResult.beneficiaire, this.prelevementResult.idNumCompte);
				} catch (DatabaseConnexionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ManagementRuleViolation e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.primaryStage.close();
			}
			break;
		case MODIFICATION:
			if (this.isSaisieValide()) {
				this.prelevementResult = this.prelevementEdite;
				this.prelevementResult.beneficiaire = this.txtBeneficiaire.getText();
				this.prelevementResult.montant = Double.parseDouble(this.txtSolde.getText());
				this.prelevementResult.dateRecurrente = this.txtDate.getText();
				try {
					this.accp.updatePrelev(prelevementResult);
				} catch (RowNotFoundOrTooManyRowsException | DataAccessException | DatabaseConnexionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.primaryStage.close();
			}
			break;
		case SUPPRESSION:
			this.prelevementResult = this.prelevementEdite;
			this.primaryStage.close();
			break;
		}

	}

	private boolean isSaisieValide() {

		return true;
	}
}
