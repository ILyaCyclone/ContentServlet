package ru.unisuite.contentservlet.config;

import ru.unisuite.imageprocessing.*;

import java.util.HashMap;
import java.util.Map;

class ImageProcessorsManager {
    static Map<ResizerType, ImageProcessor> implementations(ContentServletProperties prop) {
        Map<ResizerType, ImageProcessor> map = new HashMap<>();
        Map<String, String> allProperties = prop.getAllProperties();

        map.put(ResizerType.THUMBNAILATOR, thumbnailatorImageProcessor(allProperties));
        map.put(ResizerType.IMAGEMAGICK, imagemagickImageProcessor(allProperties));
        map.put(ResizerType.IMAGINARY, imaginaryImageProcessor(allProperties));

        return map;
    }


    private static ImageProcessor thumbnailatorImageProcessor(Map<String, String> properties) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ThumbnailatorParameters.ANTIALIASING, properties.get("imageprocessor.thumbnailator.antialiasing"));
        parameters.put(ThumbnailatorParameters.RENDERING, properties.get("imageprocessor.thumbnailator.rendering"));
        return ImageProcessorFactory.getInstance(Type.THUMBNAILATOR, parameters);
    }

    private static ImageProcessor imagemagickImageProcessor(Map<String, String> properties) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Im4javaParameters.IMAGEMAGICK_PATH, properties.get("imageprocessor.im4java.toolpath"));
        return ImageProcessorFactory.getInstance(Type.IM4JAVA, parameters);
    }

    private static ImageProcessor imaginaryImageProcessor(Map<String, String> properties) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ImaginaryParameters.BASE_URL, properties.get("imageprocessor.imaginary.base-url"));
        return ImageProcessorFactory.getInstance(Type.IMAGINARY, parameters);
    }
}
