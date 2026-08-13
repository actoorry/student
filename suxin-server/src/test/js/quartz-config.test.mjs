import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import test from 'node:test'
import { fileURLToPath } from 'node:url'

const resourcesDir = resolve(dirname(fileURLToPath(import.meta.url)), '../../main/resources')
const profiles = ['dev', 'prod', 'zjx']

function parseScalarProperties(yaml) {
  const properties = new Map()
  const path = []

  for (const rawLine of yaml.split(/\r?\n/u)) {
    if (!rawLine.trim() || rawLine.trimStart().startsWith('#') || rawLine.trim() === '---') {
      continue
    }

    const indent = rawLine.length - rawLine.trimStart().length
    const content = rawLine.trim().replace(/\s+#.*$/u, '')
    const separator = content.indexOf(':')
    if (separator < 0) {
      continue
    }

    const key = content.slice(0, separator).trim()
    const value = content.slice(separator + 1).trim()
    while (path.length && path.at(-1).indent >= indent) {
      path.pop()
    }

    const propertyPath = [...path.map((item) => item.key), key].join('.')
    if (value) {
      properties.set(propertyPath, value)
    } else {
      path.push({ indent, key })
    }
  }

  return properties
}

for (const profile of profiles) {
  test(`application-${profile}.yaml 使用现有小写 Quartz 表且不自动建表`, async () => {
    const yaml = await readFile(resolve(resourcesDir, `application-${profile}.yaml`), 'utf8')
    const properties = parseScalarProperties(yaml)

    assert.equal(properties.get('spring.quartz.job-store-type'), 'jdbc')
    assert.equal(properties.get('spring.quartz.properties.org.quartz.jobStore.tablePrefix'), 'qrtz_')
    assert.equal(properties.get('spring.quartz.jdbc.initialize-schema'), 'NEVER')
    assert.equal(properties.get('suxin.quartz.lowercase-table-names'), 'true')
  })
}
