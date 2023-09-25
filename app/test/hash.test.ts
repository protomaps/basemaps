import { expect, test } from 'vitest'
import { parseHash, createHash } from 'src/hash'

test('parses hash', () => {
  const result = parseHash("#map=0/0/0&theme=dark");
  expect(result.map).toBe("0/0/0");
  expect(result.theme).toBe("dark");
})

test('sets hash', () => {
  let result = createHash("#map=0/0/0&theme=dark", {"theme":"light"}) 
  expect(result).toBe("#map=0/0/0&theme=light")
  result = createHash("#map=0/0/0&theme=dark", {"version":"123"}) 
  expect(result).toBe("#map=0/0/0&theme=dark&version=123")
  result = createHash("#map=0/0/0&theme=dark", {"version":undefined}) 
  expect(result).toBe("#map=0/0/0&theme=dark")
})