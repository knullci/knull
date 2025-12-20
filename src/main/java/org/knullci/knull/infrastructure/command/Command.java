package org.knullci.knull.infrastructure.command;

import java.util.List;

public record Command(String executable, List<String> args) {
}
