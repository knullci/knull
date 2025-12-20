package org.knullci.knull.infrastructure.service;

import org.knullci.knull.infrastructure.command.Command;
import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.enums.Tool;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class KnullProcessRunner implements ProcessRunner {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(15);

    @Override
    public ProcessResult run(RunCommand run) {
        Command command = toCommand(run);
        return execute(command, Path.of("."), DEFAULT_TIMEOUT);
    }

    private Command toCommand(RunCommand run) {
        Tool tool = Tool.from(run.getTool());
        validateArgs(run.getArgs());

        return new Command(
                tool.executable(),
                run.getArgs()
        );
    }

    private ProcessResult execute(
            Command command,
            Path workingDir,
            Duration timeout
    ) {
        Instant start = Instant.now();
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();

        int exitCode = -1;
        boolean success = false;

        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(command.executable());
            cmd.addAll(command.args());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workingDir.toFile());
            pb.redirectErrorStream(false);

            pb.environment().clear();
            pb.environment().put("PATH", "/usr/bin:/bin");

            Process process = pb.start();

            Thread outThread = new Thread(() -> readStream(
                    process.getInputStream(), output
            ));
            Thread errThread = new Thread(() -> readStream(
                    process.getErrorStream(), error
            ));

            outThread.start();
            errThread.start();

            boolean finished = process.waitFor(
                    timeout.toSeconds(),
                    TimeUnit.SECONDS
            );

            outThread.join();
            errThread.join();

            if (!finished) {
                process.destroyForcibly();
                error.append("Process timed out");
            } else {
                exitCode = process.exitValue();
                success = exitCode == 0;
            }

        } catch (Exception e) {
            error.append(e.getMessage());
        }

        Instant end = Instant.now();

        return new ProcessResult(
                success,
                exitCode,
                output.toString(),
                error.toString(),
                start,
                end,
                Duration.between(start, end)
        );
    }

    private void validateArgs(List<String> args) {
        for (String arg : args) {
            if (arg.contains("&&")
                    || arg.contains("|")
                    || arg.contains(";")
                    || arg.contains("`")
                    || arg.contains("$(")) {
                throw new SecurityException("Forbidden operator in args: " + arg);
            }
        }
    }

    private void readStream(InputStream stream, StringBuilder target) {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(stream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                target.append(line).append("\n");
            }
        } catch (Exception ignored) {}
    }

}
