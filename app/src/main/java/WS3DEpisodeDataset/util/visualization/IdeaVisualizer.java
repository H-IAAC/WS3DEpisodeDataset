/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package WS3DEpisodeDataset.util.visualization;

import WS3DEpisodeDataset.util.IdeaHelper;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import br.unicamp.cst.util.viewer.representation.idea.IdeaPanel;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author bruno
 */
public class IdeaVisualizer extends javax.swing.JFrame {

    private IdeaPanel ideaPanel;
    private Mind mind;
    private Map<String, Integer> memoryLevels = new HashMap<>();
    
    /**
     * Creates new form IdeaVisualizer
     */
    public IdeaVisualizer(Mind mind) {
        initComponents();
        this.mind = mind;
        
        ideaPanel = new IdeaPanel(new Idea("root"), false);
        //wmp.setOpaque(true); //content panes must be opaque
        //this.setContentPane(wmp);
        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(ideaPanel);
        jPanel2.revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        list1Model = new DefaultListModel();
        jList1.setModel(list1Model);
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        String memoryName = jList1.getSelectedValue();
        Optional<Memory> selectedMem = mind.getRawMemory().getAllMemoryObjects()
                .stream().filter(m->m.getName().equalsIgnoreCase(memoryName))
                .findFirst();
        if (selectedMem.isPresent()){
            Object content = selectedMem.get().getI();
            if (content instanceof Idea)
                setIdea(new Gson().fromJson(IdeaHelper.csvPrint((Idea) content, memoryLevels.getOrDefault(memoryName, 5)), Idea.class));
            if (content instanceof List) {
                Idea show = new Idea(memoryName, null);
                show.setL((List<Idea>) content);
                setIdea(new Gson().fromJson(IdeaHelper.csvPrint(show, memoryLevels.getOrDefault(memoryName, 5)), Idea.class));
            }

        }
    }//GEN-LAST:event_jList1ValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.DefaultListModel list1Model;
    // End of variables declaration//GEN-END:variables

    public void setIdea(Idea idea){
        ideaPanel = new IdeaPanel(idea,false);
        ideaPanel.updateTree();
        ideaPanel.expandAllNodes();
        jPanel2.removeAll();
        jPanel2.add(ideaPanel);
        jPanel2.revalidate();
        revalidate();
    }

    public void addMemoryWatch(String memoryName, int printLevel){
       list1Model.addElement(memoryName);
       memoryLevels.put(memoryName, printLevel);
    }
}
