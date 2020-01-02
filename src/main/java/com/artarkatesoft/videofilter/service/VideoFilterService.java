package com.artarkatesoft.videofilter.service;

import com.artarkatesoft.videofilter.processwrapper.ProcessResult;
import com.artarkatesoft.videofilter.processwrapper.ProcessWrapper;
import com.artarkatesoft.videofilter.processwrapper.VideoProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoFilterService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${c.a.v.filter.template.stabilize.part1}")
    private String FILTER_STABILIZATION_1_TEMPLATE;

    @Value("${c.a.v.filter.template.stabilize.part2}")
    private String FILTER_STABILIZATION_2_TEMPLATE;

    @Value("${c.a.v.filter.template.antiflicker}")
    private String FILTER_ANTIFLICKER_TEMPLATE;

    @Value("${c.a.v.filter.template.cropvertical}")
    private String FILTER_CROP_V_TEMPLATE;

    @Value("${c.a.v.filter.template.crophorizontal}")
    private String FILTER_CROP_H_TEMPLATE;

    @Value("${c.a.v.filter.template.rotate}")
    private String FILTER_ROTATE_TEMPLATE;

    @Value("${c.a.v.videofiles.directory}")
    private String videofilesDirectory;

    @Value("${ffmpeg.directory}")
    private String ffmpegDirectory;

    @Value("${c.a.v.filter.sequence}")
    private List<Operation> operationSequence;


    private String ffmpegPath;// = "c:\\Users\\Art\\Downloads\\ffmpeg-20170312-58f0bbc-win64-static\\ffmpeg-20170312-58f0bbc-win64-static\\bin\\ffmpeg.exe";

    private List<Path> videoFiles;


    @Autowired
    private ProcessWrapper processWrapper;

    @PostConstruct
    private void init() throws IOException {
        if (ffmpegDirectory == null) ffmpegDirectory = "";
        ffmpegDirectory = ffmpegDirectory.trim();
        if (!ffmpegDirectory.isEmpty() && !(ffmpegDirectory.endsWith("\\") || ffmpegDirectory.endsWith("/")))
            ffmpegDirectory += "\\";
        ffmpegPath = ffmpegDirectory + "ffmpeg.exe";


        List<String> videoFormats = Arrays.asList("mp4", "avi", "vob", "ogg");
        videoFiles = Files.walk(Paths.get(videofilesDirectory))
                .filter(Files::isRegularFile)
                .filter(filePath -> {
                    String name = filePath.getFileName().toString();
                    if (!name.contains(".")) return false;
                    String extension = name.substring(name.lastIndexOf(".") + 1);
                    return videoFormats.contains(extension);
                })
                .collect(Collectors.toList());

        logger.info("Found files to convert: {}", videoFiles);


    }

    public void convert() {

        for (Path videoFile : videoFiles) {
            String fileName = videoFile.toString();
            VideoProcessResult videoProcessResult = null;
            try {
                for (Operation operation : operationSequence) {

                    switch (operation) {
                        case STABILIZE1:
                            videoProcessResult = stabilize1(fileName);
                            break;
                        case STABILIZE2:
                            videoProcessResult = stabilize2(fileName);
                            break;
                        case ANTIFLICKER:
                            videoProcessResult = antiflicker(fileName);
                            break;
                        case CROP_VERTICAL:
                            videoProcessResult = cropCentralVertical(fileName);
                            break;
                        case CROP_HORIZONTAL:
                            videoProcessResult = cropCentralHorizontal(fileName);
                            break;
                        case ROTATE_CCW:
                            videoProcessResult = rotateCCW(fileName);
                            break;
                    }

                    fileName = videoProcessResult.getResultFileName();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }

    private VideoProcessResult stabilize1(String fileName) throws IOException, InterruptedException {

        String command = String.format(FILTER_STABILIZATION_1_TEMPLATE, ffmpegPath, fileName);
        ProcessResult processResult = processWrapper.executeProcess(command);
        VideoProcessResult videoProcessResult = new VideoProcessResult(fileName, processResult);
        return videoProcessResult;

    }

    private VideoProcessResult stabilize2(String fileName) throws IOException, InterruptedException {

        String resultFileName = addOperationSuffix(fileName, "stab");

        String command = String.format(FILTER_STABILIZATION_2_TEMPLATE, ffmpegPath, fileName, resultFileName);
        ProcessResult processResult = processWrapper.executeProcess(command);
        VideoProcessResult videoProcessResult = new VideoProcessResult(resultFileName, processResult);
        return videoProcessResult;

    }

    private VideoProcessResult antiflicker(String fileName) throws IOException, InterruptedException {

        String resultFileName = addOperationSuffix(fileName, "antiflicker");

        String command = String.format(FILTER_ANTIFLICKER_TEMPLATE, ffmpegPath, fileName, fileName, resultFileName);
        ProcessResult processResult = processWrapper.executeProcess(command);
        VideoProcessResult videoProcessResult = new VideoProcessResult(resultFileName, processResult);
        return videoProcessResult;

    }

    private VideoProcessResult cropCentralVertical(String fileName) throws IOException, InterruptedException {

        String resultFileName = addOperationSuffix(fileName, "cropv");

        String command = String.format(FILTER_CROP_V_TEMPLATE, ffmpegPath, fileName, resultFileName);
        ProcessResult processResult = processWrapper.executeProcess(command);
        VideoProcessResult videoProcessResult = new VideoProcessResult(resultFileName, processResult);
        return videoProcessResult;

    }

    private VideoProcessResult cropCentralHorizontal(String fileName) throws IOException, InterruptedException {

        String resultFileName = addOperationSuffix(fileName, "croph");

        String command = String.format(FILTER_CROP_H_TEMPLATE, ffmpegPath, fileName, resultFileName);
        ProcessResult processResult = processWrapper.executeProcess(command);
        VideoProcessResult videoProcessResult = new VideoProcessResult(resultFileName, processResult);
        return videoProcessResult;
    }

    private VideoProcessResult rotateCCW(String fileName) throws IOException, InterruptedException {

        String resultFileName = addOperationSuffix(fileName, "rotate");

        String command = String.format(FILTER_ROTATE_TEMPLATE, ffmpegPath, fileName, resultFileName);
        ProcessResult processResult = processWrapper.executeProcess(command);
        VideoProcessResult videoProcessResult = new VideoProcessResult(resultFileName, processResult);
        return videoProcessResult;

    }

    private String addOperationSuffix(String filename, String suffix) {
        int lastIndexOfDot = filename.lastIndexOf(".");
        String dotExtension = filename.substring(lastIndexOfDot);
        return filename.replace(dotExtension, "_" + suffix + dotExtension);
    }


}
