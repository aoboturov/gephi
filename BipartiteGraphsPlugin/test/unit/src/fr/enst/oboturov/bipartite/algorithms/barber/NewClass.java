///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package fr.enst.oboturov.bipartite.algorithms.barber;
//
///**
// *
// * @author oboturov
// */
//public class NewClass {
//
//}
////        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
////        pc.newProject();
////        Workspace workspace = pc.getCurrentWorkspace();
////
////        Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
////
//////        Report r = new Report();
//////        container.setReport(r);
//////        container.setSource("/home/oboturov/Development/gephi/gephi-src/BipartiteGraphsPlugin/test/unit/src/fr/enst/oboturov/bipartite/algorithms/barber/davis.dl");
////
////        final File dlFile = new File("/home/oboturov/Development/gephi/gephi-src/BipartiteGraphsPlugin/test/unit/src/fr/enst/oboturov/bipartite/algorithms/barber/davis.dl");
////        final File tFile = new File("/home/oboturov/Work/collaboration/fabrice.rossi/edges.csv");
////
////
////        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
////        importController.importFile(tFile);
//////        importController.importFile(tFile,
////////                this.getClass().getResourceAsStream(),
//////                Lookup.getDefault().lookup(ImporterBuilderDL.class).buildImporter());
//////        importController.
////
////        System.out.println("Is file supported: "+importController.isFileSupported(tFile));
////
//////                new ImporterDL());
////
//////        importController.importFile(new File("/home/oboturov/Development/gephi/gephi-src/BipartiteGraphsPlugin/test/unit/src/fr/enst/oboturov/bipartite/algorithms/barber/davis.dl"));
//////        importController.importFile(new File("/home/oboturov/Work/collaboration/fabrice.rossi/edges.csv"));
////        importController.process(container, new DefaultProcessor(), workspace);
////
//////        System.out.println(r.getText());
//////        System.out.println(r.getExceptionLevel());
//////        for (Issue is : r.getIssues()) {
//////            System.out.println(is.getThrowable());
//////        }
////
////
////        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
////        GraphModel graphModel = gc.getModel(workspace);
////        System.out.println("Having edges in total: "+graphModel.getGraph().getEdgeCount());
////
//////        final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
//////        projectController.newProject();
//////        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
//////        attributeModel = attributeController.getModel();
//////        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
//////        graphModel = graphController.getModel();
//////
//    }
////
////    @After
////    public void tearDown() throws Exception {
////        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
////        projectController.closeCurrentProject();
////        graphModel = null;
////        attributeModel = null;
////        containerLoader = null;
////    }
//
