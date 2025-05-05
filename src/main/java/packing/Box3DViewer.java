package packing;


import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import packing.algorithm.RecursiveBDModule;

import java.util.*;

public class Box3DViewer extends Application {

    static class BoxSpec {
        double width, height, depth;
        double x, y, z;

        BoxSpec(double w, double h, double d, double x, double y, double z) {
            this.width = w;
            this.height = h;
            this.depth = d;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BoxSpec b)) return false;
            return width == b.width && height == b.height && depth == b.depth;
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, height, depth);
        }
    }

    private double anchorX, anchorY;
    private double angleX = 0;
    private double angleY = 0;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Group world = new Group();
        root.getChildren().add(world);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-1000);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);

        Scene scene = new Scene(root, 200, 150, true);
        scene.setFill(Color.LIGHTGRAY);
        scene.setCamera(camera);

        //(42,39,9,4),
        int L0 = 42;
        int W0 = 39;
        int n = 0;
        int N0 = 3;
        int l0 = 9;
        int w0 = 4;


        // Container size
        double containerWidth = L0, containerHeight = 300, containerDepth = W0;

        // Shift world so (0,0,0) = container's bottom-left-front corner
        world.setTranslateX(-containerWidth / 2);
        world.setTranslateY(containerHeight / 2);
        world.setTranslateZ(-containerDepth / 2);

        // Transparent container centered in world
        Box container = new Box(containerWidth, containerHeight, containerDepth);
        container.setTranslateX(containerWidth / 2);
        container.setTranslateY(-containerHeight / 2);
        container.setTranslateZ(containerDepth / 2);
        PhongMaterial transparentMaterial = new PhongMaterial();
        transparentMaterial.setDiffuseColor(Color.rgb(200, 200, 200, 0.2));
        container.setMaterial(transparentMaterial);
        container.setDrawMode(DrawMode.LINE);
        world.getChildren().add(container);



        RecursiveBDModule solver = new RecursiveBDModule();
        RecursiveBDModule.Result result = solver.palletLoading(L0, W0, n, N0, l0, w0);

        List<BoxSpec> boxes = new ArrayList<>();
        for (RecursiveBDModule.Tuple4<Integer, Integer, Integer, Integer> block : result.getBlocks()) {
            System.out.println("(" + block.getFirst() + ", " + block.getSecond() + ", " +
                    block.getThird() + ", " + block.getFourth() + ")");
            boxes.add(new BoxSpec(block.getThird(), 10, block.getFourth(), block.getFirst(), 0, block.getSecond()));
        }


        Map<BoxSpec, PhongMaterial> materialMap = new HashMap<>();
        Random rand = new Random();

        for (BoxSpec b : boxes) {
            Box box = new Box(b.width, b.height, b.depth);
            // JavaFX centers boxes — offset by half the dimension
            box.setTranslateX(b.x + b.width / 2);
            box.setTranslateY(-b.y - b.height / 2);
            box.setTranslateZ(b.z + b.depth / 2);

            materialMap.computeIfAbsent(
                    new BoxSpec(b.width, b.height, b.depth, 0, 0, 0),
                    k -> new PhongMaterial(Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()))
            );

            box.setMaterial(materialMap.get(new BoxSpec(b.width, b.height, b.depth, 0, 0, 0)));
            world.getChildren().add(box);
        }

        // Add rotation controls
        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        world.getTransforms().addAll(rotateX, rotateY);

        scene.setOnMousePressed((MouseEvent event) -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
        });

        scene.setOnMouseDragged((MouseEvent event) -> {
            angleY += event.getSceneX() - anchorX;
            angleX -= event.getSceneY() - anchorY;
            rotateX.setAngle(angleX);
            rotateY.setAngle(angleY);
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
        });

        // Scroll to zoom
        Translate zoom = new Translate(0, 0, 0);
        world.getTransforms().add(zoom);

        scene.setOnScroll((ScrollEvent event) -> {
            double delta = event.getDeltaY();
            zoom.setZ(zoom.getZ() + delta);
        });

        stage.setTitle("3D Box Viewer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
// mvn clean javafx:run -X