package com.codecool.server;

import com.codecool.dao.IMentorDao;
import com.codecool.dao.ISessionDao;
import com.codecool.dao.IStudentDao;
import com.codecool.model.Student;
import com.codecool.server.helper.CommonHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class MentorHandler implements HttpHandler {
    private IStudentDao studentDao;
    private ISessionDao sessionDao;
    private CommonHelper commonHelper;
    private IMentorDao mentorDao;

    public MentorHandler(IStudentDao studentDao, ISessionDao sessionDao, CommonHelper commonHelper, IMentorDao mentorDao) {
        this.studentDao = studentDao;
        this.sessionDao = sessionDao;
        this.commonHelper = commonHelper;
        this.mentorDao = mentorDao;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "";
        String method = httpExchange.getRequestMethod();
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        HttpCookie cookie;
        if (method.equals("GET")) {
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
        }
        commonHelper.sendResponse(httpExchange, response);
    }

    private String handleRequest(HttpExchange httpExchange, int userId, String method) throws IOException {
        String response = "";
        URI uri = httpExchange.getRequestURI();
        Map<String, String> parsedUri = commonHelper.parseURI(uri.getPath());
        String action = "index";
        int studentId = 0;
        if(!parsedUri.isEmpty()) {
            action = parsedUri.keySet().iterator().next();
            studentId = Integer.parseInt(parsedUri.get(action));
        }
        switch (action) {
            case "index":
                response = index(userId);
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                break;
            case "add":
                response = add(method, httpExchange);
                break;
            case "edit":
                response = edit(studentId, method, httpExchange);
                break;
            case "delete":
                delete(studentId, httpExchange);
                break;
            default:
                response = index(userId);
                break;
        }
        return response;
    }

    private String index(int userId) {
        String fullName = String.format("%s %s", mentorDao.getMentor(userId).getFirstName(),
                mentorDao.getMentor(userId).getLastName());
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/mentor.twig");
        JtwigModel model = JtwigModel.newModel();
        List<Student> students = studentDao.getAllStudents();
        model.with("students", students);
        model.with("fullName", fullName);
        String response = template.render(model);
        return response;
    }

    private String quests() {
        return "";
    }

    private String artifacts() {
        return "";
    }

    private String add(String method, HttpExchange httpExchange) {
        return "";
    }

    private String edit(int studentId, String method, HttpExchange httpExchange) {
        return "";
    }

    private String delete(int studentId, HttpExchange httpExchange) {
        return "";
    }
}