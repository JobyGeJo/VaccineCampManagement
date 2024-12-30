package org.myapplication.exceptions;

import org.myapplication.tools.ColoredOutput;

public class InvalidRequestException extends RuntimeException {

  public int status;

  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(String message, int status) {
    super(message);
    this.status = status;
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(ColoredOutput.yellow(message), cause);
  }
}
