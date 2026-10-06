// Test env lacks Node globals that some libs reference.
(window as unknown as Record<string, unknown>)['global'] ??= window;
