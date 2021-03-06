/**
 * <h2>This is the 'Exports' class, it allows the user to select orders for the list of existing ones, and export them as XML.</h2>
 * 
 * @author Rachel Slater
 * @since December 2016
 * 
 */

package gc01coursework.admin_functionality;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * The Class ExportOrders.
 * This class exports XML data files based on user selection.
 */
public class ExportOrders implements Initializable {
	
	private ArrayList<String> tablesWithOrders, dateStampForOrders, selectableOrders;
	@FXML private GridPane exportOrdersGridPane;
	@FXML private Button selectExport, removeExport, exportButton, cancelButton;
	@FXML private ObservableList<String> selectedExports;

	/** 
	 * This method parses 'allOrders.xml' and builds ArrayLists and table and date data. 
	 * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// Checking if which tables have orders!
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = null;

		try {
			doc = documentBuilder.parse("src/gc01coursework/xml_data/allOrders.xml");
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}

		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("order");

		// Getting table numbers:
		tablesWithOrders = new ArrayList<String>();

		for (int i = 0; i < nList.getLength(); i++) {
			Node order = nList.item(i);
			String table = order.getFirstChild().getFirstChild().getNodeValue();
			tablesWithOrders.add(table);
		}

		// Getting date stamps of the existing orders:
		dateStampForOrders = new ArrayList<String>();
		NodeList list = doc.getElementsByTagName("date");

		for (int j = 0; j < tablesWithOrders.size(); j++) {
			if (list != null && list.getLength() > 0) {
				NodeList subList = list.item(j).getChildNodes();

				if (subList != null && subList.getLength() > 0) {
					String dateStamp = subList.item(0).getNodeValue();
					dateStampForOrders.add(dateStamp);
				}
			}
		}
		populateExistingOrders(tablesWithOrders, dateStampForOrders);
	}

	/**
	 * Populate existing orders.
	 * This method populates the ListViews with data, and handles the selection of orders. 
	 * @param listOfTablesWithOrders the list of tables with orders
	 * @param listOfDateStampsForOrders the list of date stamps for orders
	 */
	private void populateExistingOrders(ArrayList<String> listOfTablesWithOrders, ArrayList<String> listOfDateStampsForOrders) {
		// Populating orders for selection column:
		selectableOrders = new ArrayList<String>();
		String eachOrder;

		for (int k = 0; k < listOfTablesWithOrders.size(); k++) {
			eachOrder = "Order @ Table " + listOfTablesWithOrders.get(k) + " (created on " + listOfDateStampsForOrders.get(k) + ")";
			selectableOrders.add(eachOrder);
		}

		final ObservableList<String> orders = FXCollections.observableArrayList(selectableOrders);
		ListView<String> ordersForSelection = new ListView<String>(orders);

		exportOrdersGridPane.add(ordersForSelection, 0, 1);

		// Handling selection of orders for export:

		selectedExports = FXCollections.observableArrayList();
		ListView<String> selections = new ListView<>(selectedExports);
		exportOrdersGridPane.add(selections, 2, 1);

		selectExport.setOnAction((ActionEvent event) -> {
			String potential = ordersForSelection.getSelectionModel().getSelectedItem();
			if (potential != null) {
				ordersForSelection.getSelectionModel().clearSelection();
				selectedExports.add(potential);
				orders.remove(potential);
			}
		});

		removeExport.setOnAction((ActionEvent event) -> {
			String undo = selections.getSelectionModel().getSelectedItem();
			if (undo != null) {
				selections.getSelectionModel().clearSelection();
				selectedExports.remove(undo);

				if (!orders.contains(undo)) {
					orders.add(undo);
				}
			}
		});
	}

	
	/**
	 * Export as XML.
	 * The file is exported as '.xml' as it's extension. 
	 * 
	 * @param event triggered when the 'exportButton' is clicked. 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TransformerFactoryConfigurationError the transformer factory configuration error
	 * @throws TransformerException the transformer exception
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 */
	@FXML
	public void exportAsXML(ActionEvent event) throws IOException, TransformerFactoryConfigurationError, TransformerException, ParserConfigurationException, SAXException {
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Image");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("(*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        Window primaryStage = null;
		File file = fileChooser.showSaveDialog(primaryStage);
        String fileName = file.getAbsolutePath();
        
		//Creating The Export File:
		File xmlFile = new File(fileName);
		DocumentBuilderFactory exportFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder exportBuilder = exportFactory.newDocumentBuilder();
		Document xmlExport = exportBuilder.newDocument();	
		Element initialize = xmlExport.createElement("orders");
		Node ftt = xmlExport.getParentNode();
		xmlExport.appendChild(initialize);
		
		//Finishing the XML creation. 
		OutputFormat outFormat = new OutputFormat(xmlExport);
		outFormat.setIndenting(true);
		FileOutputStream outStream = new FileOutputStream(xmlFile, true);
		XMLSerializer serializer = new XMLSerializer(outStream, outFormat);
		serializer.serialize(xmlExport);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
		DOMSource source = new DOMSource(xmlExport);
		StreamResult console = new StreamResult(System.out);
		transformer.transform(source, console);
		System.out.println("\nXML DOM Created Successfully..");
		
		addTheSelectedOrders(fileName);
	}
	
	/**
	 * Adds the the selected orders.
	 * The selected orders are appended to the exported file. 
	 *
	 * @param file the file
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TransformerException the transformer exception
	 */
	private void addTheSelectedOrders(String file) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		
		// Reading the Existing Orders Data in order to match the selected orders.
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse("src/gc01coursework/xml_data/allOrders.xml");
		doc.getDocumentElement().normalize();
		
		DocumentBuilderFactory exportingFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder exportingBuilder = exportingFactory.newDocumentBuilder();
		Document export = exportingBuilder.parse(file);
		
		// Extracting details necessary to match orders.
		for (int i = 0; i < selectedExports.size(); i++) {
			String eachOrder = selectedExports.get(i);
			String[] parts = eachOrder.split(" \\(");
			String tableIdentifier = parts[0];
			String dateIdentifier = parts[1];

			String[] gettingTable = tableIdentifier.split("e ");
			String tableNumber = gettingTable[1];
			String[] gettingDate = dateIdentifier.split("n ");
			String[] date = gettingDate[1].split("\\)");
			String dateCreated = date[0];

			NodeList orderList = doc.getElementsByTagName("order");

			for (int j = 0; j < orderList.getLength(); j++) {
				Node order = orderList.item(j);
				Node theDate = order.getFirstChild().getNextSibling();
				if (order.getFirstChild().getFirstChild().getNodeValue().equals(tableNumber) && theDate.getFirstChild().getNodeValue().equals(dateCreated)) {					
					Element root = export.getDocumentElement();
			        Node selected = export.importNode(order, true);
					root.appendChild(selected);
				}		
			}
			
			DOMSource source = new DOMSource(export);
			TransformerFactory tFactory = TransformerFactory.newInstance(); 
			Transformer transformer = tFactory.newTransformer();
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result); 
		}	
		Stage stage = (Stage) exportButton.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	public void cancel(ActionEvent event) {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}
}


