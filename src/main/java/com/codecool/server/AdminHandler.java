package com.codecool.server;

import com.codecool.dao.IClassDao;
import com.codecool.dao.IMentorDao;
import com.codecool.dao.ISessionDao;
import com.codecool.hasher.PasswordHasher;
import com.codecool.model.ClassGroup;
import com.codecool.model.Mentor;
import com.codecool.model.Student;
import com.codecool.model.UserCredentials;
import com.codecool.server.helper.CommonHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminHandler implements HttpHandler {
    private IMentorDao mentorDao;
    private ISessionDao sessionDao;
    private CommonHelper commonHelper;
    private IClassDao classDao;
    private PasswordHasher passwordHasher;


    public AdminHandler(IMentorDao mentorDao, ISessionDao sessionDao, CommonHelper commonHelper, IClassDao classDao) {
        this.mentorDao = mentorDao;
        this.sessionDao = sessionDao;
        this.commonHelper = commonHelper;
        this.classDao = classDao;
        this.passwordHasher = new PasswordHasher();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "";
        String method = httpExchange.getRequestMethod();
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        HttpCookie cookie;
        if (cookieStr != null) {
            cookie = HttpCookie.parse(cookieStr).get(0);
            if (sessionDao.isCurrentSession(cookie.getValue())) {
                int userId = sessionDao.getUserIdBySessionId(cookie.getValue());
                response = handleRequest(httpExchange, userId, method);
            } else {
                commonHelper.redirectToUserPage(httpExchange, "/");
            }
        } else {
            commonHelper.redirectToUserPage(httpExchange, "/");
        }
        commonHelper.sendResponse(httpExchange, response);
    }

    private String handleRequest(HttpExchange httpExchange, int userId, String method) throws IOException {
        String response = "";
        URI uri = httpExchange.getRequestURI();
        Map<String, String> parsedUri = commonHelper.parseURI(uri.getPath());
        String action = "index";
        int mentorId = 0;
        if(!parsedUri.isEmpty()) {
            action = parsedUri.keySet().iterator().next();
            mentorId = Integer.parseInt(parsedUri.get(action));
        }
        switch (action) {
            case "index":
                response = index(userId);
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                break;
            case "add":
                response = add(method, httpExchange);
                break;
            case "view":
                response = view(mentorId, httpExchange);
                break;
            case "edit":
                response = edit(mentorId, method, httpExchange);
                break;
            case "delete":
                delete(mentorId, httpExchange);
                break;
            default:
                response = index(userId);
                break;
        }
        return response;
    }

    private String view(int mentorId, HttpExchange httpExchange) throws IOException {
        String response = "";
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/mentorForm.twig");
        JtwigModel model = JtwigModel.newModel();
        String disabled = "disabled";
        Mentor mentor = mentorDao.getMentor(mentorId);
        List<Student> students = classDao.getAllStudentsFromClass(mentor);
        List<ClassGroup> mentorClasses = classDao.getMentorClasses(mentorId);
        model.with("mentorClasses", mentorClasses);
        model.with("mentor", mentor);
        model.with("students", students);
        model.with("disabled", disabled);
        httpExchange.sendResponseHeaders(200, response.length());
        response = template.render(model);
        return response;
    }

    private String index(int userId) {
        String fullName = String.format("%s %s", mentorDao.getMentor(userId).getFirstName(),
                mentorDao.getMentor(userId).getLastName());
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/admin.twig");
        JtwigModel model = JtwigModel.newModel();
        List<Mentor> mentors = mentorDao.getAllMentors();
        model.with("mentors", mentors);
        model.with("fullName", fullName);
        String response = template.render(model);
        return response;
    }

    private String add(String method, HttpExchange httpExchange) throws IOException {
        String response = "";
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/mentorForm.twig");
        JtwigModel model = JtwigModel.newModel();
        final int EMPTY_MENTOR = -1;
        List<ClassGroup> emptyClasses = classDao.getAllUnassignedClasses();
        model.with("emptyClasses", emptyClasses);
        if (method.equals("GET")) {
            String operation = "add";
            model.with("operation", operation);
            httpExchange.sendResponseHeaders(200, response.length());
            response = template.render(model);
        }

        if (method.equals("POST")) {
            InputStreamReader inputStreamReader = new InputStreamReader(httpExchange.getRequestBody(), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String formData = bufferedReader.readLine();
            List<Integer> mentorClasses = new ArrayList<>();
            Map<String, String> inputs = commonHelper.parseFormData(formData);
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                if (entry.getKey().matches("class.")) {
                    mentorClasses.add(Integer.parseInt(entry.getValue()));
                }
            }

            String firstName = inputs.get("firstName");
            String lastName = inputs.get("lastName");
            String type = inputs.get("type");
            String mentorLogin = inputs.get("mentorLogin");
            String mentorPassword = inputs.get("mentorPassword");
            String email = inputs.get("email");

            String salt = passwordHasher.getRandomSalt();
            mentorPassword = passwordHasher.hashPassword(mentorPassword + salt);

            UserCredentials userCredentials = new UserCredentials(mentorLogin, mentorPassword);
            Mentor mentor = new Mentor.MentorBuilder()
                    .setFirstName(firstName)
                    .setLastName(lastName)
                    .setType(type)
                    .setUserCredentials(userCredentials)
                    .setEmail(email).build();
            mentorDao.addMentor(mentor);
            mentorDao.insertMentorInCredentialsQuery(mentor, salt);

            int mentorId = mentorDao.getNewMentorId();
            for (int classId : mentorClasses) {
                classDao.addMentorToClass(mentorId, classId);
            }
            commonHelper.redirectToUserPage(httpExchange, "/admin");
        }
    return response;
    }

    private String edit(int mentorId, String method, HttpExchange httpExchange) throws IOException {
        String response = "";
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/mentorForm.twig");
        JtwigModel model = JtwigModel.newModel();
        if (method.equals("GET")) {
            Mentor mentor = mentorDao.getMentor(mentorId);
            List<Student> students = classDao.getAllStudentsFromClass(mentor);
            List<ClassGroup> mentorClasses = classDao.getMentorClasses(mentorId);
            List<ClassGroup> emptyClasses = classDao.getAllUnassignedClasses();
            String operationDisabled = "disabled";
            String checked = "checked";
            String operation = "edit/" + mentorId;
            model.with("mentorClasses", mentorClasses);
            model.with("emptyClasses", emptyClasses);
            model.with("mentor", mentor);
            model.with("students", students);
            model.with("operationDisabled", operationDisabled);
            model.with("checked", checked);
            model.with("operation", operation);
            httpExchange.sendResponseHeaders(200, response.length());
            response = template.render(model);
        }

        if (method.equals("POST")) {
            InputStreamReader inputStreamReader = new InputStreamReader(httpExchange.getRequestBody(), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String formData = bufferedReader.readLine();
            List<Integer> mentorClasses = new ArrayList<>();
            Map<String, String> inputs = commonHelper.parseFormData(formData);
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                if (entry.getKey().matches("class.")) {
                    mentorClasses.add(Integer.parseInt(entry.getValue()));
                }
            }

            String firstName = inputs.get("firstName");
            String lastName = inputs.get("lastName");
            String type = inputs.get("type");
            String email = inputs.get("email");

            Mentor mentor = new Mentor.MentorBuilder()
                    .setId(mentorId)
                    .setFirstName(firstName)
                    .setLastName(lastName)
                    .setType(type)
                    .setEmail(email).build();
            mentorDao.updateMentor(mentor);

            classDao.updateMentorClasses(mentorId, mentorClasses);
            commonHelper.redirectToUserPage(httpExchange, "/admin");
        }
        return response;
    }

    private void delete(int mentorId, HttpExchange httpExchange) throws IOException {
        mentorDao.removeMentor(mentorId);
        commonHelper.redirectToUserPage(httpExchange, "/admin");
    }

}
