package com.codecool.dao.sql;

import com.codecool.dao.IClassDao;
import com.codecool.model.ClassGroup;
import com.codecool.model.Mentor;
import com.codecool.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClassSQL implements IClassDao {
    private ConnectionPool connectionPool;

    public ClassSQL(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void addClass(ClassGroup classGroup) {
        String query = "INSERT INTO classes (class_name) VALUES (?)";

        try {
            Connection connection = connectionPool.getConnection();
            insertClassData(query, connection, classGroup);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void insertClassData(String query, Connection connection, ClassGroup classGroup) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, classGroup.getClassName());

            stmt.executeUpdate();
        }
    }

    @Override
    public void updateClass(ClassGroup classGroup) {
        String query = "UPDATE classes SET mentor_id = ?, class_name = ? WHERE id = ?";

        try {
            Connection connection = connectionPool.getConnection();
            updateClassData(query, connection, classGroup);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void updateClassData(String query, Connection connection, ClassGroup classGroup) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, classGroup.getMentorId());
            stmt.setString(2, classGroup.getClassName());
            stmt.setInt(3, classGroup.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void removeClass(ClassGroup classGroup) {
        String questsDataQuery = "DELETE FROM classes WHERE id = ?";

        try {
            Connection connection = connectionPool.getConnection();
            deleteClassData(questsDataQuery, connection, classGroup);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void deleteClassData(String query, Connection connection, ClassGroup classGroup) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, classGroup.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public List<ClassGroup> getAllClasses() {
        List<ClassGroup> listOfClasses = new ArrayList<>();
        String query = "SELECT * FROM classes";

        try {
            Connection connection = connectionPool.getConnection();
            prepareClassesListQuery(listOfClasses, query, connection);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }


        return listOfClasses;
    }

    private void prepareClassesListQuery(List<ClassGroup> listOfClasses, String query, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            executeClassesListQuery(listOfClasses, stmt);
        }
    }

    private void executeClassesListQuery(List<ClassGroup> listOfClasses, PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String className = rs.getString("class_name");
                int mentorId = rs.getInt("mentor_id");

                ClassGroup classGroup = new ClassGroup(id, className, mentorId);

                listOfClasses.add(classGroup);
            }
        }
    }

    @Override
    public ClassGroup getClass(int classId) {
        String query = "SELECT * FROM classes WHERE id = ?";
        ClassGroup classGroup = null;
        try {
            Connection connection = connectionPool.getConnection();
            classGroup = prepareMentorByIdQuery(classGroup, query, connection, classId);
            connectionPool.releaseConnection(connection);
            return classGroup;
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
        throw  new RuntimeException("No class by that id");
    }

    private ClassGroup prepareMentorByIdQuery(ClassGroup classGroup, String query, Connection connection, int classId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, classId);
            classGroup = executeMentorByIdQuery(classGroup, classId, stmt);
        }
        return classGroup;
    }

    private ClassGroup executeMentorByIdQuery(ClassGroup classGroup, int classId, PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String className = rs.getString("class_name");
                int mentorId = rs.getInt("mentor_id");


                classGroup = new ClassGroup(id, className, mentorId);
            }
            return classGroup;
        }
    }

    @Override
    public String getClassName(Student student) {
        String query = "SELECT class_name WHERE id = ?";
        ClassGroup classGroup = null;
        try {
            Connection connection = connectionPool.getConnection();
            classGroup = prepareMentorByIdQuery(classGroup, query, connection, student.getClassId());
            connectionPool.releaseConnection(connection);
            return classGroup.getClassName();
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
        throw  new RuntimeException("No class by that id");
    }

    @Override
    public void addStudentToClass(Student student, ClassGroup classGroup) {
        String query = "UPDATE students_profiles SET class_id = ? WHERE student_id = ?";

        try {
            Connection connection = connectionPool.getConnection();
            updateStudentClassId(query, connection, classGroup, student);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void updateStudentClassId(String query, Connection connection, ClassGroup classGroup, Student student) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, classGroup.getId());
            stmt.setInt(2, student.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void addMentorToClass(Mentor mentor, ClassGroup classGroup) {
        String query = "UPDATE classes SET mentor_id = ? WHERE class_id = ?";

        try {
            Connection connection = connectionPool.getConnection();
            updateStudentClassId(query, connection, classGroup, mentor);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void updateStudentClassId(String query, Connection connection, ClassGroup classGroup, Mentor mentor) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, mentor.getId());
            stmt.setInt(2, classGroup.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public List<Student> getAllStudentsFromClass(Mentor mentor) {
        List<Student> students = new ArrayList<>();
        try {
            Connection connection = connectionPool.getConnection();
            addStudents(students, connection, mentor);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
        return students;
    }

    private void addStudents(List<Student> students, Connection connection, Mentor mentor) throws SQLException {
        try(PreparedStatement stmt = connection.prepareStatement(
                "SELECT users.id, users.type, users.first_name, users.last_name,\n" +
                        "users.email, cred.login, cred.password, profile.coins, profile.experience,\n" +
                        "profile.class_id FROM users\n" +
                        "JOIN user_credentials AS cred ON users.id = cred.user_id\n" +
                        "JOIN students_profiles AS profile on users.id = profile.student_id\n" +
                        "WHERE profile.class_id IN ((SELECT id FROM classes WHERE mentor_id = ?))")) {
            stmt.setInt(1, mentor.getId());
            addStudentToList(stmt, students);
        }
    }

    private void addStudentToList(PreparedStatement stmt, List<Student> students) throws SQLException {
        try (ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                Student student = buildSingleStudent(resultSet);
                students.add(student);
            }
        }
    }

    private Student buildSingleStudent(ResultSet resultSet) throws SQLException {
        UserBuilder userBuilder = new UserBuilder();
        return userBuilder.buildSingleStudent(resultSet);
    }
}