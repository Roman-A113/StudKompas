package com.example.studkompas;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

import android.content.Context;

import com.example.studkompas.model.Campus;
import com.example.studkompas.model.GraphNode;
import com.example.studkompas.model.PathWithTransition;
import com.example.studkompas.utils.GraphMaker;
import com.example.studkompas.utils.GraphManager;
import com.example.studkompas.utils.GraphTestHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphManagerTest {

    private GraphManager graphManager;

    @Mock
    private Context mockContext;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Campus mockCampus = new Campus("Test", "test", "Address", Map.of(1, 123));
        graphManager = new GraphManager(mockContext, mockCampus);
    }

    // ====================== БАЗОВЫЕ ТЕСТЫ ======================

    @Test
    public void testDijkstra_SimplePath() {
        graphManager.CampusGraph = GraphMaker.createSimpleGraph();
        GraphNode start = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        GraphNode end = GraphTestHelper.getNodeById(graphManager.CampusGraph, "3");

        PathWithTransition path = graphManager.getPathBetweenTwoNodes(start, end, false);

        assertNotNull("Путь должен быть найден", path);
        assertEquals("Должен быть 1 этаж", 1, path.segmentedPath.size());
        assertTrue("Должен содержать этаж 1", path.segmentedPath.containsKey("1"));

        List<GraphNode> nodes = path.segmentedPath.get("1").get(0);
        assertEquals("Должно быть 3 узла", 3, nodes.size());
        assertEquals("Первый узел A", "1", nodes.get(0).id);
        assertEquals("Второй узел B", "2", nodes.get(1).id);
        assertEquals("Третий узел C", "3", nodes.get(2).id);
    }

    @Test(expected = RuntimeException.class)
    public void testDijkstra_NoPath() {
        graphManager.CampusGraph = GraphMaker.createSimpleGraph();
        GraphNode isolated = GraphTestHelper.createIsolatedNode("4", "Изолированный", "1", 0.8f, 0.2f);
        graphManager.CampusGraph.get("1").put("4", isolated);

        GraphNode start = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        GraphNode end = GraphTestHelper.getNodeById(graphManager.CampusGraph, "4");

        graphManager.getPathBetweenTwoNodes(start, end, false);
    }

    @Test
    public void testDijkstra_ShortestPathSelection() {
        graphManager.CampusGraph = GraphMaker.createShortestPathGraph();
        GraphNode A = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        GraphNode D = GraphTestHelper.getNodeById(graphManager.CampusGraph, "4");

        PathWithTransition path = graphManager.getPathBetweenTwoNodes(A, D, false);

        List<GraphNode> nodes = path.segmentedPath.get("1").get(0);
        assertEquals("Должно быть 3 узла", 3, nodes.size());
        assertEquals("Первый узел A", "1", nodes.get(0).id);
        assertEquals("Второй узел C (кратчайший путь)", "3", nodes.get(1).id); // C, а не B!
        assertEquals("Третий узел D", "4", nodes.get(2).id);
    }

    // ====================== МЕЖЭТАЖНЫЕ ПЕРЕХОДЫ ======================

    @Test
    public void testDijkstra_InterFloorEdges() {
        graphManager.CampusGraph = GraphMaker.createMultiFloorGraph();
        GraphNode lift1 = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        GraphNode room = GraphTestHelper.getNodeById(graphManager.CampusGraph, "3");

        PathWithTransition path = graphManager.getPathBetweenTwoNodes(lift1, room, false);

        assertNotNull("Путь должен быть найден", path);
        assertEquals("Должно быть 2 этажа", 2, path.segmentedPath.size());
        assertEquals("Должен быть 1 переход", 1, path.transitionNodes.size());

        assertTrue("Путь должен содержать лифт",
                GraphTestHelper.pathContainsNode(path, "Лифт"));
        assertTrue("Должен быть переход через лифт",
                GraphTestHelper.pathHasTransitionType(path, "Лифт"));
    }

    @Test(expected = RuntimeException.class)
    public void testDijkstra_IgnoreElevators() {
        graphManager.CampusGraph = GraphMaker.createElevatorOnlyGraph();
        GraphNode start = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        GraphNode end = GraphTestHelper.getNodeById(graphManager.CampusGraph, "5");

        // Единственный путь через лифт, должен выбросить исключение
        graphManager.getPathBetweenTwoNodes(start, end, true);
    }

    @Test
    public void testDijkstra_AllowElevators() {
        graphManager.CampusGraph = GraphMaker.createElevatorOnlyGraph();
        GraphNode start = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        GraphNode end = GraphTestHelper.getNodeById(graphManager.CampusGraph, "5");

        PathWithTransition path = graphManager.getPathBetweenTwoNodes(start, end, false);

        assertNotNull("Путь должен быть найден при разрешенных лифтах", path);
        assertEquals("Должно быть 2 этажа", 2, path.segmentedPath.size());
    }

    // ====================== ГРАНИЧНЫЕ СЛУЧАИ ======================

    @Test
    public void testDijkstra_SameStartAndEnd() {
        graphManager.CampusGraph = GraphMaker.createSingleNodeGraph();
        GraphNode node = GraphTestHelper.getAllNodes(graphManager.CampusGraph).get(0);

        PathWithTransition path = graphManager.getPathBetweenTwoNodes(node, node, false);

        assertNotNull("Путь из точки в ту же точку должен существовать", path);
        assertEquals("Должен быть 1 этаж", 1, path.segmentedPath.size());
        assertEquals("Должен быть 1 узел", 1, path.segmentedPath.get("1").get(0).size());
        assertEquals("Нет переходов", 0, path.transitionNodes.size());
    }

    @Test
    public void testDijkstra_CircularGraph() {
        graphManager.CampusGraph = GraphMaker.createCircularGraph();
        GraphNode A = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        GraphNode D = GraphTestHelper.getNodeById(graphManager.CampusGraph, "4");

        PathWithTransition path = graphManager.getPathBetweenTwoNodes(A, D, false);

        assertNotNull("Путь в циклическом графе должен быть найден", path);
        List<GraphNode> nodes = path.segmentedPath.get("1").get(0);
        assertEquals("Должно быть 4 узла", 4, nodes.size());

        // Проверяем правильный порядок (должен избегать лишних циклов)
        assertEquals("A -> B -> C -> D", "1", nodes.get(0).id);
        assertEquals("1", "2", nodes.get(1).id);
        assertEquals("2", "3", nodes.get(2).id);
        assertEquals("3", "4", nodes.get(3).id);
    }

    @Test
    public void testDijkstra_BoundaryCoordinates() {
        graphManager.CampusGraph = GraphMaker.createBoundaryCoordinatesGraph();
        GraphNode corner1 = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        GraphNode corner2 = GraphTestHelper.getNodeById(graphManager.CampusGraph, "2");

        PathWithTransition path = graphManager.getPathBetweenTwoNodes(corner1, corner2, false);

        assertNotNull("Путь для крайних координат должен быть найден", path);
        assertEquals("Должно быть 3 узла", 3, path.segmentedPath.get("1").get(0).size());
    }

    // ====================== ТЕСТЫ ДЛЯ CRUD ОПЕРАЦИЙ ======================

    @Test
    public void testAddNode() {
        graphManager.CampusGraph = GraphMaker.createSimpleGraph();
        int initialCount = GraphTestHelper.countNodes(graphManager.CampusGraph);

        graphManager.addNode("1", 0.3f, 0.3f, "Новая вершина");

        int newCount = GraphTestHelper.countNodes(graphManager.CampusGraph);
        assertEquals("Должна добавиться одна вершина", initialCount + 1, newCount);

        GraphNode newNode = graphManager.findNodeAt(0.3f, 0.3f, "1");
        assertNotNull("Новая вершина должна быть найдена", newNode);
        assertEquals("Новая вершина", newNode.name);
    }

    @Test
    public void testDeleteNode() {
        graphManager.CampusGraph = GraphMaker.createSimpleGraph();
        int initialCount = GraphTestHelper.countNodes(graphManager.CampusGraph);
        GraphNode nodeToDelete = GraphTestHelper.getNodeById(graphManager.CampusGraph, "2");

        graphManager.deleteNode("1", nodeToDelete);

        int newCount = GraphTestHelper.countNodes(graphManager.CampusGraph);
        assertEquals("Должна удалиться одна вершина", initialCount - 1, newCount);

        GraphNode found = graphManager.findNodeAt(0.5f, 0.5f, "1");
        assertNull("Удаленная вершина не должна находиться", found);

        // Проверяем, что ребра удалены у соседних вершин
        GraphNode A = GraphTestHelper.getNodeById(graphManager.CampusGraph, "1");
        assertFalse("Ребро к удаленной вершине должно быть удалено",
                A.edges.contains("2"));
    }

    @Test
    public void testFindNodeAt() {
        graphManager.CampusGraph = GraphMaker.createSimpleGraph();

        GraphNode node = graphManager.findNodeAt(0.1f, 0.1f, "1");
        assertNotNull("Узел должен быть найден", node);
        assertEquals("1", node.id);
        assertEquals("A", node.name);

        node = graphManager.findNodeAt(0.8f, 0.2f, "1");
        assertNull("Несуществующий узел не должен быть найден", node);
    }

    // ====================== ТЕСТ ПРИВАТНОГО МЕТОДА ======================

    @Test
    public void testBuildPathWithTransitions_MissingNode() throws Exception {
        // Подготовка тестовых данных
        Map<String, GraphNode> allNodes = new HashMap<>();
        GraphNode A = new GraphNode("1", "A", "1", new float[]{0.1f, 0.1f});
        GraphNode C = new GraphNode("3", "C", "2", new float[]{0.3f, 0.3f});

        allNodes.put("1", A);
        allNodes.put("3", C);
        // Узел B отсутствует!

        Map<String, String> parents = new HashMap<>();
        parents.put("1", null);
        parents.put("3", "2");  // Родитель ссылается на несуществующий узел "2"

        Method method = GraphManager.class.getDeclaredMethod(
                "buildPathWithTransitions",
                GraphNode.class,
                Map.class,
                Map.class
        );
        method.setAccessible(true);

        try {
            method.invoke(graphManager, C, parents, allNodes);
            fail("Должно было выбросить RuntimeException при отсутствии узла");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            assertTrue("Должно быть RuntimeException", cause instanceof RuntimeException);
            assertTrue("Сообщение должно содержать 'несуществующий'",
                    cause.getMessage().contains("несуществующий"));
        }
    }
}