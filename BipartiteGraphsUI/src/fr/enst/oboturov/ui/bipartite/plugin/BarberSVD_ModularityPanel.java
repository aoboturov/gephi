/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enst.oboturov.ui.bipartite.plugin;

final class BarberSVD_ModularityPanel extends javax.swing.JPanel {

    public double getQ_delta() {
        try {
            Double dQ = Double.parseDouble(this.qTresholdFormattedTextField.getText());
            if ( dQ <= 0D) {
                throw new IllegalArgumentException("dQ > 0");
            }
            return dQ;
        } catch (Exception ex) {
            return 0.001D;
        }
    }

    public void setQ_delta(final double dQ) {
        this.qTresholdFormattedTextField.setText(String.format("%.3f", dQ));
    }

    BarberSVD_ModularityPanel() {
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        qTresholdLabel = new javax.swing.JLabel();
        qTresholdFormattedTextField = new javax.swing.JFormattedTextField();

        org.openide.awt.Mnemonics.setLocalizedText(qTresholdLabel, org.openide.util.NbBundle.getMessage(BarberSVD_ModularityPanel.class, "BarberSVD_ModularityPanel.qTresholdLabel.text")); // NOI18N

        qTresholdFormattedTextField.setColumns(6);
        qTresholdFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
        qTresholdFormattedTextField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                qTresholdFormattedTextFieldPropertyChange(evt);
            }
        });
        qTresholdFormattedTextField.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                qTresholdFormattedTextFieldVetoableChange(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(qTresholdLabel)
                    .addComponent(qTresholdFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(qTresholdLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qTresholdFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void qTresholdFormattedTextFieldVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_qTresholdFormattedTextFieldVetoableChange
        // TODO add your handling code here:
    }//GEN-LAST:event_qTresholdFormattedTextFieldVetoableChange

    private void qTresholdFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_qTresholdFormattedTextFieldPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_qTresholdFormattedTextFieldPropertyChange

    void load() {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(BarberSVD_ModularityPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(BarberSVD_ModularityPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
    }

    void store() {
        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(BarberSVD_ModularityPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        // NbPreferences.forModule(BarberSVD_ModularityPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField qTresholdFormattedTextField;
    private javax.swing.JLabel qTresholdLabel;
    // End of variables declaration//GEN-END:variables
}
