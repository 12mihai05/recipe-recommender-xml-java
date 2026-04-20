package org.seweb.reciperecommenderxmljava.service;

import org.seweb.reciperecommenderxmljava.model.Recipe;
import org.seweb.reciperecommenderxmljava.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class XmlService {

    @Value("${app.xml.recipes.path:src/main/resources/xml/recipes.xml}")
    private String recipesXmlPath;

    @Value("${app.xml.users.path:src/main/resources/xml/users.xml}")
    private String usersXmlPath;

    @Value("${app.xml.recipes.schema:src/main/resources/xml/recipes.xsd}")
    private String recipesSchemaPath;

    @Value("${app.xml.users.schema:src/main/resources/xml/users.xsd}")
    private String usersSchemaPath;

    public boolean validateRecipesXml() {
        return validateXmlAgainstSchema(recipesXmlPath, recipesSchemaPath);
    }

    public boolean validateUsersXml() {
        return validateXmlAgainstSchema(usersXmlPath, usersSchemaPath);
    }

    private boolean validateXmlAgainstSchema(String xmlPath, String schemaPath) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(schemaPath));
            Validator validator = schema.newValidator();
            validator.validate(new javax.xml.transform.stream.StreamSource(new File(xmlPath)));
            return true;
        } catch (SAXException | IOException e) {
            System.err.println("Validation error: " + e.getMessage());
            return false;
        }
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        try {
            Document doc = parseXmlFile(recipesXmlPath);
            if (doc == null) return recipes;

            NodeList recipeNodes = doc.getElementsByTagName("recipe");
            for (int i = 0; i < recipeNodes.getLength(); i++) {
                Element recipeElement = (Element) recipeNodes.item(i);
                Recipe recipe = parseRecipeFromElement(recipeElement);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading recipes: " + e.getMessage());
        }
        return recipes;
    }

    public List<AppUser> getAllUsers() {
        List<AppUser> users = new ArrayList<>();
        try {
            Document doc = parseXmlFile(usersXmlPath);
            if (doc == null) return users;

            NodeList userNodes = doc.getElementsByTagName("user");
            for (int i = 0; i < userNodes.getLength(); i++) {
                Element userElement = (Element) userNodes.item(i);
                AppUser user = parseUserFromElement(userElement);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading users: " + e.getMessage());
        }
        return users;
    }

    public AppUser getFirstUser() {
        List<AppUser> users = getAllUsers();
        return users.isEmpty() ? null : users.get(0);
    }

    public List<Recipe> getRecipesBySkill(String skillLevel) {
        List<Recipe> filteredRecipes = new ArrayList<>();

        try {
            Document doc = parseXmlFile(recipesXmlPath);
            if (doc == null) return filteredRecipes;

            List<String> allowedDifficulties = getRecipeDifficultiesForSkillLevel(skillLevel);
            StringBuilder difficultyPredicate = new StringBuilder();
            for (int i = 0; i < allowedDifficulties.size(); i++) {
                if (i > 0) {
                    difficultyPredicate.append(" or ");
                }
                difficultyPredicate.append("difficulty='").append(allowedDifficulties.get(i)).append("'");
            }

            String expression = "//recipe[" + difficultyPredicate + "]";
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Recipe recipe = parseRecipeFromElement((Element) nodes.item(i));
                if (recipe != null) {
                    filteredRecipes.add(recipe);
                }
            }
        } catch (XPathExpressionException e) {
            System.err.println("Error querying recipes by skill: " + e.getMessage());
        }

        return filteredRecipes;
    }

    public List<Recipe> getRecipesBySkillAndCuisine(String skillLevel, String cuisineType) {
        List<Recipe> filtered = new ArrayList<>();

        try {
            Document doc = parseXmlFile(recipesXmlPath);
            if (doc == null) return filtered;

            List<String> allowedDifficulties = getRecipeDifficultiesForSkillLevel(skillLevel);
            StringBuilder difficultyPredicate = new StringBuilder();
            for (int i = 0; i < allowedDifficulties.size(); i++) {
                if (i > 0) {
                    difficultyPredicate.append(" or ");
                }
                difficultyPredicate.append("difficulty='").append(allowedDifficulties.get(i)).append("'");
            }

            String expression = "//recipe[(" + difficultyPredicate + ") and (cuisineType1='" + cuisineType + "' or cuisineType2='" + cuisineType + "')]";
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Recipe recipe = parseRecipeFromElement((Element) nodes.item(i));
                if (recipe != null) {
                    filtered.add(recipe);
                }
            }
        } catch (XPathExpressionException e) {
            System.err.println("Error querying recipes by skill and cuisine: " + e.getMessage());
        }

        return filtered;
    }

    public Recipe getRecipeById(int id) {
        try {
            Document doc = parseXmlFile(recipesXmlPath);
            if (doc == null) return null;

            String expression = "//recipe[id=" + id + "]";
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);

            if (node instanceof Element) {
                return parseRecipeFromElement((Element) node);
            }
        } catch (XPathExpressionException e) {
            System.err.println("Error querying recipe by id: " + e.getMessage());
        }

        return null;
    }

    public List<Recipe> getRecipesByCuisine(String cuisineType) {
        List<Recipe> filtered = new ArrayList<>();

        try {
            Document doc = parseXmlFile(recipesXmlPath);
            if (doc == null) return filtered;

            String expression = "//recipe[cuisineType1='" + cuisineType + "' or cuisineType2='" + cuisineType + "']";
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Recipe recipe = parseRecipeFromElement((Element) nodes.item(i));
                if (recipe != null) {
                    filtered.add(recipe);
                }
            }
        } catch (XPathExpressionException e) {
            System.err.println("Error querying recipes by cuisine: " + e.getMessage());
        }

        return filtered;
    }

    public void saveRecipe(Recipe recipe) {
        try {
            Document doc = parseXmlFile(recipesXmlPath);
            if (doc == null) return;

            Element rootElement = doc.getDocumentElement();

            Element newRecipeElement = doc.createElement("recipe");

            Element idElement = doc.createElement("id");
            idElement.setTextContent(String.valueOf(recipe.getId()));
            newRecipeElement.appendChild(idElement);

            Element titleElement = doc.createElement("title");
            titleElement.setTextContent(recipe.getTitle());
            newRecipeElement.appendChild(titleElement);

            Element cuisineType1Element = doc.createElement("cuisineType1");
            cuisineType1Element.setTextContent(recipe.getCuisineType1());
            newRecipeElement.appendChild(cuisineType1Element);

            Element cuisineType2Element = doc.createElement("cuisineType2");
            cuisineType2Element.setTextContent(recipe.getCuisineType2());
            newRecipeElement.appendChild(cuisineType2Element);

            Element difficultyElement = doc.createElement("difficulty");
            difficultyElement.setTextContent(recipe.getDifficulty());
            newRecipeElement.appendChild(difficultyElement);

            rootElement.appendChild(newRecipeElement);

            writeXmlFile(doc, recipesXmlPath);
        } catch (Exception e) {
            System.err.println("Error saving recipe: " + e.getMessage());
        }
    }

    public void saveUser(AppUser user) {
        try {
            Document doc = parseXmlFile(usersXmlPath);
            if (doc == null) return;

            Element rootElement = doc.getDocumentElement();

            Element newUserElement = doc.createElement("user");

            Element firstNameElement = doc.createElement("firstName");
            firstNameElement.setTextContent(user.getFirstName());
            newUserElement.appendChild(firstNameElement);

            Element lastNameElement = doc.createElement("lastName");
            lastNameElement.setTextContent(user.getLastName());
            newUserElement.appendChild(lastNameElement);

            Element skillLevelElement = doc.createElement("skillLevel");
            skillLevelElement.setTextContent(user.getSkillLevel());
            newUserElement.appendChild(skillLevelElement);

            Element preferredCuisineElement = doc.createElement("preferredCuisineType");
            preferredCuisineElement.setTextContent(user.getPreferredCuisineType());
            newUserElement.appendChild(preferredCuisineElement);

            rootElement.appendChild(newUserElement);

            writeXmlFile(doc, usersXmlPath);
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    private Document parseXmlFile(String filePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new File(filePath));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.println("Error parsing XML file: " + e.getMessage());
            return null;
        }
    }

    private void writeXmlFile(Document doc, String filePath) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, false);
        transformerFactory.setFeature(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, false);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(doc), new StreamResult(new File(filePath)));
    }

    private Recipe parseRecipeFromElement(Element element) {
        try {
            int id = Integer.parseInt(getElementText(element, "id"));
            String title = getElementText(element, "title");
            String cuisineType1 = getElementText(element, "cuisineType1");
            String cuisineType2 = getElementText(element, "cuisineType2");
            String difficulty = getElementText(element, "difficulty");

            return new Recipe(id, title, cuisineType1, cuisineType2, difficulty);
        } catch (Exception e) {
            System.err.println("Error parsing recipe: " + e.getMessage());
            return null;
        }
    }

    private AppUser parseUserFromElement(Element element) {
        try {
            String firstName = getElementText(element, "firstName");
            String lastName = getElementText(element, "lastName");
            String skillLevel = getElementText(element, "skillLevel");
            String preferredCuisineType = getElementText(element, "preferredCuisineType");

            return new AppUser(firstName, lastName, skillLevel, preferredCuisineType);
        } catch (Exception e) {
            System.err.println("Error parsing user: " + e.getMessage());
            return null;
        }
    }

    private String getElementText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    private List<String> getRecipeDifficultiesForSkillLevel(String skillLevel) {
        List<String> difficulties = new ArrayList<>();
        difficulties.add("Beginner");

        if ("Intermediate".equals(skillLevel) || "Advanced".equals(skillLevel)) {
            difficulties.add("Intermediate");
        }

        if ("Advanced".equals(skillLevel)) {
            difficulties.add("Advanced");
        }

        return difficulties;
    }
}








