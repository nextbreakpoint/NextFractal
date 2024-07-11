package com.nextbreakpoint.nextfractal.runtime.javafx.utils;

import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.Clip;
import com.nextbreakpoint.nextfractal.core.common.CoreFactory;
import com.nextbreakpoint.nextfractal.core.common.FileManager;
import com.nextbreakpoint.nextfractal.core.common.Plugins;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.encode.Encoder;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.javafx.Bitmap;
import com.nextbreakpoint.nextfractal.core.javafx.BrowseBitmap;
import com.nextbreakpoint.nextfractal.core.javafx.GridItemRenderer;
import com.nextbreakpoint.nextfractal.core.javafx.UIFactory;
import com.nextbreakpoint.nextfractal.core.javafx.UIPlugins;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindEncoder;
import static com.nextbreakpoint.nextfractal.core.javafx.UIPlugins.tryFindFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log
public class ApplicationUtils {
    private static final String PROPERTY_DIRECTORY_WORKSPACE = "com.nextbreakpoint.nextfractal.directory.workspace";
    private static final String PROPERTY_DIRECTORY_EXAMPLES = "com.nextbreakpoint.nextfractal.directory.examples";
    private static final String WORKSPACE_DIRECTORY = ".nextfractal";

    public static void printPlugins() {
        Plugins.factories().forEach(plugin -> log.fine("Found plugin " + plugin.getId()));
    }

    public static Either<BrowseBitmap> createBitmap(File file, RendererSize size) {
        return Command.of(FileManager.loadBundle(file))
                .flatMap(bundle -> Command.of(tryFindFactory(bundle.getSession().getPluginId()))
                .flatMap(factory -> Command.of(() -> factory.createBitmap(bundle.getSession(), size))))
                .execute();
    }

    public static Either<GridItemRenderer> createRenderer(Bitmap bitmap) {
        return Command.of(tryFindFactory(((Session) bitmap.getProperty("session")).getPluginId()))
                .flatMap(factory -> Command.of(() -> factory.createRenderer(bitmap)))
                .execute();
    }

    public static void loadStyleSheets(Scene scene) {
        loadResource("/theme.css").optional().ifPresent(resourceURL -> scene.getStylesheets().add((resourceURL)));

        UIPlugins.factories().map(factory -> loadResource(factory, factory.getId().toLowerCase() + ".css"))
                .forEach(maybeURL -> maybeURL.optional().ifPresent(resourceURL -> scene.getStylesheets().add((resourceURL))));
    }

    public static Either<String> loadResource(String resourceName) {
        return Command.of(() -> Objects.requireNonNull(ApplicationUtils.class.getResource(resourceName)).toExternalForm()).execute();
    }

    public static Optional<? extends Encoder> createEncoder(String format) {
        return tryFindEncoder(format)
                .observe()
                .onFailure(e -> log.log(Level.WARNING, "Can't find encoder for format " + format, e))
                .get()
                .optional();
    }

    public static Either<Session> createSession(CoreFactory factory) {
        return Command.of(() -> Objects.requireNonNull(factory.createSession()))
                .execute()
                .observe()
                .onFailure(e -> log.log(Level.WARNING, "Can't create session for " + factory.getId(), e))
                .get();
    }

    public static ExportSession createExportSession(Encoder encoder, Session session, List<Clip> clips, RendererSize size, File file) throws IOException {
        final String uuid = UUID.randomUUID().toString();
        final File tmpFile = File.createTempFile("export-" + uuid, ".dat");
        final List<Clip> clipList = encoder.isVideoSupported() ? clips : new LinkedList<>();
        return new ExportSession(uuid, session, clipList, file, tmpFile, size, 400, encoder);
    }

    public static ImageView createImageView(String imageResourceName, int size) {
        try (final InputStream stream = Objects.requireNonNull(ApplicationUtils.class.getResourceAsStream(imageResourceName))) {
            final ImageView image = new ImageView(new Image(stream));
            image.setSmooth(true);
            image.setFitWidth(size);
            image.setFitHeight(size);
            return image;
        } catch (Exception e) {
            throw new RuntimeException("Can't load resource " + imageResourceName, e);
        }
    }

    public static int computeRenderSize(Rectangle2D visualBounds) {
        return (int) Math.rint(Math.max(visualBounds.getWidth() * 0.5, 800));
    }

    public static int computeOptimalFontSize(Screen screen) {
        int size = 8;

        if (screen.getDpi() > 100 || screen.getVisualBounds().getWidth() > 1200) {
            size = 12;
        }

        if (screen.getDpi() > 200 || screen.getVisualBounds().getWidth() > 2400) {
            size = 16;
        }

        return size;
    }

    public static void checkJavaCompiler() {
        Command.of(() -> Objects.requireNonNull(ToolProvider.getSystemJavaCompiler()))
                .execute()
                .observe()
                .onFailure(e -> showCompilerAlert())
                .get();
    }

    public static String getApplicationName() {
        return "NextFractal 2.3.0";
    }

    public static String getNoticeMessage() {
        return """

                NextFractal 2.3.0

                https://github.com/nextbreakpoint/nextfractal

                Copyright 2015-2024 Andrea Medeghini

                NextFractal is an application for creating fractals and other graphics artifacts.

                NextFractal is free software: you can redistribute it and/or modify
                it under the terms of the GNU General Public License as published by
                the Free Software Foundation, either version 3 of the License, or
                (at your option) any later version.

                NextFractal is distributed in the hope that it will be useful,
                but WITHOUT ANY WARRANTY; without even the implied warranty of
                MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
                GNU General Public License for more details.

                You should have received a copy of the GNU General Public License
                along with NextFractal.  If not, see <http://www.gnu.org/licenses/>.

                """;
    }

    public static File getWorkspace() {
        File path = new File(getDefaultDirectoryWorkspace());
        log.info("workspace = " + path.getAbsolutePath());
        if (path.getParentFile().canWrite() && !path.exists()) {
            if (!path.mkdirs()) {
                log.severe("Can't create directory: " + path.getParentFile());
            }
        }
        if (!path.canWrite()) {
            log.severe("Can't write into workspace: " + path.getAbsolutePath());
        }
        if (!path.canRead()) {
            log.severe("Can't read from workspace: " + path.getAbsolutePath());
        }
        return path;
    }

    public static File getExamples() {
        File path = new File(getDefaultDirectoryExamples());
        log.info("examples = " + path.getAbsolutePath());
        if (!path.canRead()) {
            log.severe("Can't read from examples: " + path.getAbsolutePath());
        }
        return path;
    }

    private static Either<String> loadResource(UIFactory factory, String resourceName) {
        return factory.loadResource("/" + resourceName)
                .observe()
                .onFailure(e -> log.log(Level.WARNING, "Can't load style sheet " + resourceName, e))
                .get();
    }

    private static void showCompilerAlert() {
        final ButtonType buttonType = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        final Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        dialog.setGraphic(ApplicationUtils.createImageView("/icon-errors.png", 32));
        dialog.setTitle("Warning");
        dialog.setHeaderText("Java compiler not found in classpath");
        dialog.setContentText("The compiler is recommended to reduce computation time");
        dialog.showAndWait();
    }

    private static String getDefaultDirectoryWorkspace() {
        return System.getProperty(PROPERTY_DIRECTORY_WORKSPACE, getWorkspaceDefaultValue().getAbsolutePath());
    }

    private static String getDefaultDirectoryExamples() {
        return System.getProperty(PROPERTY_DIRECTORY_EXAMPLES, getExamplesDefaultValue().getAbsolutePath());
    }

    private static File getWorkspaceDefaultValue() {
        return new File(System.getProperty("user.home"), WORKSPACE_DIRECTORY);
    }

    private static File getExamplesDefaultValue() {
        return new File(System.getProperty("user.home"));
    }
}
