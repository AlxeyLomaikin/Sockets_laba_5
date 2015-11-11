package Server;

import model.doctor;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by User on 27.10.2015.
 */
public class ServerImpl {
    private  int freeID;
    private final ArrayList<Socket> clients = new ArrayList<Socket>();
    private final ArrayList<doctor> doctors = new ArrayList<doctor>();

    public ServerImpl() throws Exception{
        this.freeID = 1;
        this.ReadXML();
    }

    public ArrayList<doctor> getData (){
        return this.doctors;
    }

    public ArrayList<Socket> getClients (){
        return this.clients;
    }

    public void addClient(String clientName) {
    }

    //Поиск по ID
    public ArrayList<doctor> findByID(int ID) {
        ArrayList<doctor> foundedDocs = new ArrayList<doctor>();
        for (doctor doctor : doctors) {
            if (doctor.getID() == ID) {
                foundedDocs.add(doctor);
                return foundedDocs;
            }
        }
        return foundedDocs;
    }

    //Поиск по возрасту
    public ArrayList<doctor> findByAge(int age) {
        ArrayList<doctor> foundedDocs = new ArrayList<doctor>();
        for (doctor doctor : doctors) {
            if (doctor.getAge() == age) {
                foundedDocs.add(doctor);
            }
        }
        return foundedDocs;
    }

    //Поиск по имени
    public ArrayList<doctor> findByName(String name) {
        ArrayList<doctor> foundedDocs = new ArrayList<doctor>();
        for (doctor doctor : doctors) {
            if (name.equals(doctor.getName())) {
                foundedDocs.add(doctor);
            }
        }
        return foundedDocs;
    }

    //Поиск по фамилии
    public ArrayList<doctor> findBySurname(String surname) {
        ArrayList<doctor> foundedDocs = new ArrayList<doctor>();
        for (doctor doctor : doctors) {
            if (surname.equals(doctor.getSurname())) {
                foundedDocs.add(doctor);
            }
        }
        return foundedDocs;
    }

    //Поиск по профессии
    public ArrayList<doctor> findByOccupation(String occupation) {
        ArrayList<doctor> foundedDocs = new ArrayList<doctor>();
        for (doctor doctor : doctors) {
            if (occupation.equals(doctor.getOccupation())) {
                foundedDocs.add(doctor);
            }
        }
        return foundedDocs;
    }


    //получаем Index элемента по ID
    //Index==-1 - ID not found
    private int GetIndex (int ID)
    {
        int Index = -1;
        for (int m = 0; m<this.doctors.size(); m++)
            if(this.doctors.get(m).getID() == ID) {
                Index = m;
                break;
            }
        return Index;
    }

    //очистка данных на сервере
    private boolean delAll() {
        if (doctors.isEmpty()) return false;
        else {
            this.doctors.clear();
            File docFile = new File("Doctors.xml");
            //удаляем файл
            if (docFile.exists() && docFile.length() != 0)
                docFile.delete();
            return true;
        }
    }

    //добавление объекта в список
    public int add(doctor doctor) {
        doctors.add(doctor);
        int Ind = doctors.size() - 1;
        //присваиваем ID новому объекту
        doctors.get(Ind).setID(freeID);
        int newID = freeID;
        freeID++;
        //изменяем XML
        try{
            addDoctorXML();
        }catch(Exception e){
        }
        return  newID;
    }

    //редактирование объекта
    public boolean edit(int ID, doctor doctor) {
        //получаем индекс по ID
        int Index = GetIndex(ID);
        if (Index!=-1) {
            doctors.set(Index, doctor);
            //изменяем XML
            try{
                editDoctorXML(doctor);
            }catch(Exception e){}
            return true;
        }
        else
            return false;
    }

    public ArrayList<doctor> findByAll(String id, String name, String surname,
                                       String occ, String age) {
        ArrayList<doctor> FoundDoctors = new ArrayList<doctor>();
        FoundDoctors.addAll(doctors);
        try {
            //введеное поле не пусто
            boolean b1 = !((id == null) || (id.length() == 0));
            boolean b2 = !((name == null) || (name.length() == 0));
            boolean b3 = !((surname == null) || (surname.length() == 0));
            boolean b4 = !((occ == null) || (occ.length() == 0));
            boolean b5 = !((age == null) || (age.length() == 0));
            //Поиск по id
            if (b1) {
                for (int k = 0; k < FoundDoctors.size(); k++) {
                    if (FoundDoctors.get(k).getID() != Integer.parseInt(id)) {
                        FoundDoctors.remove(k);
                        k--;
                    }
                }
            }
            //Поиск по имени
            if (b2) {
                for (int k = 0; k < FoundDoctors.size(); k++) {
                    if (!FoundDoctors.get(k).getName().equals(name)) {
                        FoundDoctors.remove(k);
                        k--;
                    }
                }
            }
            //Поиск по фамилии
            if (b3) {
                for (int k = 0; k < FoundDoctors.size(); k++) {
                    if (!FoundDoctors.get(k).getSurname().equals(surname)) {
                        FoundDoctors.remove(k);
                        k--;
                    }
                }
            }
            //Поиск по профессии
            if (b4) {
                for (int k = 0; k < FoundDoctors.size(); k++) {
                    if (!(FoundDoctors.get(k).getOccupation().equals(occ))) {
                        FoundDoctors.remove(k);
                        k--;
                    }
                }
            }
            //Поиск по возрасту
            if (b5) {
                for (int k = 0; k < FoundDoctors.size(); k++) {
                    if (FoundDoctors.get(k).getAge() != Integer.parseInt(age)) {
                        FoundDoctors.remove(k);
                        k--;
                    }

                }
            }
        }catch (NumberFormatException n){
        }
       finally {
            return FoundDoctors;
        }
    }

    //Удаление объекта
    public boolean delElement (int ID) {
        //получаем индекс по ID
        int Index = GetIndex(ID);
        if (Index!=-1) {
            doctors.remove(Index);
            //изменяем XML
            try{
                delDoctorXML(ID);
            }catch (Exception e){
            }
            return true;
        }
        else
            return false;
    }

    //сохраняем DOM-дерево в файл
    private void WiriteDOMDoc(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("Doctors.xml"));
        //каждый тег с новой строки
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
    }

    /*добавление элементов в DOM-дерево
   doc - исходный документ
   startIndex - индекс объекта в списке,
    с которого нужно добавлять */
    private Document AddToDOM(Document doc, int startIndex) {
        Document updDoc = doc;
        Node root = doc.getFirstChild();
        for (int k = startIndex; k < doctors.size(); k++) {
            Element curDoctor = doc.createElement("doctor");
            root.appendChild(curDoctor);

            Attr attr = doc.createAttribute("id");
            attr.setValue(String.valueOf(doctors.get(k).getID()));
            curDoctor.setAttributeNode(attr);

            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(doctors.get(k).getName()));
            curDoctor.appendChild(name);

            Element surname = doc.createElement("surname");
            surname.appendChild(doc.createTextNode(doctors.get(k).getSurname()));
            curDoctor.appendChild(surname);

            Element occupation = doc.createElement("occupation");
            occupation.appendChild(doc.createTextNode(doctors.get(k).getOccupation()));
            curDoctor.appendChild(occupation);

            Element age = doc.createElement("age");
            age.appendChild(doc.createTextNode(String.valueOf(doctors.get(k).getAge())));
            curDoctor.appendChild(age);
        }
        return updDoc;
    }

    /*Создание xml из существующего списка
   Если файл существует - будет перезаписан*/
    private void CreateXML() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("Doctors");
        doc.appendChild(root);
        doc = AddToDOM(doc, 0);
        WiriteDOMDoc(doc);
    }

    //add element in file
    private void addDoctorXML() throws Exception {
        File docFile = new File("Doctors.xml");

        //all ID in file
        HashSet<Integer> FileID = new HashSet<Integer>();

         /*Valid==false - if file is incorrect:
        - has many identical ID
        - has ID <0
         */
        boolean Valid = true;
        //old version of file
        boolean OldFile = false;

        //if file is exist and isn't empty
        if (docFile.exists() && docFile.length() != 0) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(docFile);
            NodeList nList = doc.getElementsByTagName("doctor");

            //getting list of ID from file
            for (int k = 0; k < nList.getLength(); k++) {
                if (nList.item(k).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nList.item(k);
                    int id = Integer.parseInt(el.getAttribute("id"));
                    //file is incorrect
                    if (id < 0 | (!FileID.add(id))) {
                        Valid = false;
                        break;
                    }
                }
            }
            if (Valid) {
                //проверяем соответствуют ли ID в файле ID на сервере
                if (FileID.size() == doctors.size())
                    for (doctor doctor : doctors) {
                        OldFile = (!FileID.contains(doctor.getID()));
                        if (OldFile)
                            break;
                    }
                else OldFile = true;
                if (!OldFile) {
                    //Index of element which added in list
                    int startIndex = doctors.size()-1;
                    doc = AddToDOM(doc, startIndex);
                    WiriteDOMDoc(doc);
                }
                //if file is old - rewrite it
                else CreateXML();
            }
            //if file is incorrect - rewrite it
            else CreateXML();
        }
        //if file is empty - create new file
        else CreateXML();
    }

    /*Delete element in file  by ID
      if file is incorrect  and has many identical ID,
      then the first of them will changed*/
    private void delDoctorXML(int ID)  throws Exception{
        File docFile = new File("Doctors.xml");
        //Is ID found in file
        boolean success = false;
        if (docFile.exists() && docFile.length() != 0) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(docFile);
            NodeList nList = doc.getElementsByTagName("doctor");
            for (int k = 0; k < nList.getLength(); k++) {
                if (nList.item(k).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nList.item(k);
                    if (Integer.parseInt(el.getAttribute("id")) == ID) {
                        nList.item(k).getParentNode().removeChild(nList.item(k));
                        success = true;
                        break;
                    }
                }
            }
            //If element was deleted - rewriting file
            if (success)
                WiriteDOMDoc(doc);
        }
    }

    /*Edit element in file  by ID
      if file is incorrect  and has many identical ID,
      then the first of them will changed*/
    private void editDoctorXML(doctor doctor) throws Exception {
        File docFile = new File("Doctors.xml");
        //Is ID found in file
        boolean success = false;
        if (docFile.exists() && docFile.length() != 0) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(docFile);
            NodeList nList = doc.getElementsByTagName("doctor");
            for (int k = 0; k < nList.getLength(); k++) {
                if (nList.item(k).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nList.item(k);
                    if (Integer.parseInt(el.getAttribute("id")) == doctor.getID()) {
                        ((Element) nList.item(k)).getElementsByTagName("name").item(0).setTextContent(doctor.getName());
                        ((Element) nList.item(k)).getElementsByTagName("surname").item(0).setTextContent(doctor.getSurname());
                        ((Element) nList.item(k)).getElementsByTagName("occupation").item(0).setTextContent(doctor.getOccupation());
                        ((Element) nList.item(k)).getElementsByTagName("age").item(0).setTextContent(String.valueOf(doctor.getAge()));
                        success = true;
                        break;
                    }
                }
            }
            //If element was edited - rewriting file
            if (success)
                WiriteDOMDoc(doc);
        }
    }

    //Reading data from file
    private void ReadXML() throws Exception{
        File docFile = new File("Doctors.xml");
        if (docFile.exists() && docFile.length() != 0) {
            if (!doctors.isEmpty())
                this.delAll();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(docFile);
            NodeList nList = doc.getElementsByTagName("doctor");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nNode;
                    el.setAttribute("id", String.valueOf(temp+1));
                    String name = el.getElementsByTagName("name").item(0).getTextContent();
                    String surname = el.getElementsByTagName("surname").item(0).getTextContent();
                    String occupation = el.getElementsByTagName("occupation").item(0).getTextContent();
                    int age = Integer.parseInt(el.getElementsByTagName("age").item(0).getTextContent());
                    doctor doctor = new doctor(name, surname, occupation, age);
                    doctor.setID (temp+1);
                    doctors.add(doctor);
                    this.freeID = (temp + 2);
                }
            }
            //Rewrite file to synchronize with data on server
            WiriteDOMDoc(doc);
        }
    }
}
