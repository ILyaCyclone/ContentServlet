package ru.unisuite.contentservlet.config;

import ru.unisuite.imageprocessing.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class ImageProcessorsManager {
    private ImageProcessorsManager(){}

    static Map<ResizerType, ImageProcessor> implementations(PropertyResolver propertyResolver) {
        Map<ResizerType, ImageProcessor> implementations = new EnumMap<>(ResizerType.class);

        implementations.put(ResizerType.THUMBNAILATOR, thumbnailatorImageProcessor(propertyResolver));

        imagemagickImageProcessor(propertyResolver).ifPresent(imagemagick -> implementations.put(ResizerType.IMAGEMAGICK, imagemagick));
        imaginaryImageProcessor(propertyResolver).ifPresent(imaginary -> implementations.put(ResizerType.IMAGINARY, imaginary));
        return implementations;
    }


    private static ImageProcessor thumbnailatorImageProcessor(PropertyResolver propertyResolver) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ThumbnailatorParameters.ANTIALIASING, propertyResolver.resolve("imageprocessor.thumbnailator.antialiasing"));
        parameters.put(ThumbnailatorParameters.RENDERING, propertyResolver.resolve("imageprocessor.thumbnailator.rendering"));
        return ImageProcessorFactory.getInstance(Type.THUMBNAILATOR, parameters);
    }

    private static Optional<ImageProcessor> imagemagickImageProcessor(PropertyResolver propertyResolver) {
        String imagemagickPath = propertyResolver.resolve("imageprocessor.im4java.toolpath");
        if (imagemagickPath == null) {
            return Optional.empty();
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Im4javaParameters.IMAGEMAGICK_PATH, imagemagickPath);
        return Optional.of(ImageProcessorFactory.getInstance(Type.IM4JAVA, parameters));
    }

    private static Optional<ImageProcessor> imaginaryImageProcessor(PropertyResolver propertyResolver) {
        String imaginaryBaseUrl = propertyResolver.resolve("imageprocessor.imaginary.base-url");
        if (imaginaryBaseUrl == null) {
            return Optional.empty();
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ImaginaryParameters.BASE_URL, imaginaryBaseUrl);
        return Optional.of(ImageProcessorFactory.getInstance(Type.IMAGINARY, parameters));
    }
}
