package model.data;

public class PrelevementAutomatique {

	public int idPrelev;
	public double montant;
	public String dateRecurrente;
	public String beneficiaire;
	public int idNumCompte;

	public PrelevementAutomatique(int idPrelev, double montant, String dateRecurrente, String Beneficiaire, int idNumCompte ) {
		super();
		this.idPrelev = idPrelev;
		this.montant = montant;
		this.dateRecurrente = dateRecurrente;
		this.beneficiaire = Beneficiaire;
		this.idNumCompte = idNumCompte;
	}

	public PrelevementAutomatique(PrelevementAutomatique p) {
		this(p.idPrelev, p.montant, p.dateRecurrente, p.beneficiaire, p.idNumCompte);
	}

	public PrelevementAutomatique() {
		this(-1000, 0, null, "", -1000);
	}

	@Override
	public String toString() {
		return this.idPrelev +  " Montant : " + this.montant + " | Date : " + this.dateRecurrente + " | Bénéficiaire : " + this.beneficiaire + " | Compte : " + this.idNumCompte;
	}
}
