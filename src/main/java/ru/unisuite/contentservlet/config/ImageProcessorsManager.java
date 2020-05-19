package ru.unisuite.contentservlet.config;

import ru.unisuite.imageprocessing.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class ImageProcessorsManager {
    static Map<ResizerType, ImageProcessor> implementations(ContentServletProperties prop) {
        Map<ResizerType, ImageProcessor> map = new HashMap<>();
        Map<String, String> properties = prop.values();

        map.put(ResizerType.THUMBNAILATOR, thumbnailatorImageProcessor(properties));

        imagemagickImageProcessor(properties).map(imagemagick -> map.put(ResizerType.IMAGEMAGICK, imagemagick));
        imaginaryImageProcessor(properties).map(imaginary -> map.put(ResizerType.IMAGINARY, imaginary));

        return map;
    }


    private static ImageProcessor thumbnailatorImageProcessor(Map<String, String> properties) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ThumbnailatorParameters.ANTIALIASING, properties.get("imageprocessor.thumbnailator.antialiasing"));
        parameters.put(ThumbnailatorParameters.RENDERING, properties.get("imageprocessor.thumbnailator.rendering"));
        return ImageProcessorFactory.getInstance(Type.THUMBNAILATOR, parameters);
    }

    private static Optional<ImageProcessor> imagemagickImageProcessor(Map<String, String> properties) {
        String imagemagickPath = properties.get("imageprocessor.im4java.toolpath");
        if (imagemagickPath == null) {
            return Optional.empty();
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Im4javaParameters.IMAGEMAGICK_PATH, imagemagickPath);
        return Optional.of(ImageProcessorFactory.getInstance(Type.IM4JAVA, parameters));
    }

    private static Optional<ImageProcessor> imaginaryImageProcessor(Map<String, String> properties) {
        String imaginaryBaseUrl = properties.get("imageprocessor.imaginary.base-url");
        if (imaginaryBaseUrl == null) {
            return Optional.empty();
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ImaginaryParameters.BASE_URL, imaginaryBaseUrl);
        return Optional.of(ImageProcessorFactory.getInstance(Type.IMAGINARY, parameters));
    }
}
